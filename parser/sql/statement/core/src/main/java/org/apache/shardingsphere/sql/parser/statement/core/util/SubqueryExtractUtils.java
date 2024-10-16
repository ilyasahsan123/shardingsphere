/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sql.parser.statement.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Subquery extract utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryExtractUtils {
    
    /**
     * Get subquery segment from SelectStatement.
     *
     * @param selectStatement SelectStatement
     * @return subquery segment collection
     */
    public static Collection<SubquerySegment> getSubquerySegments(final SelectStatement selectStatement) {
        List<SubquerySegment> result = new LinkedList<>();
        extractSubquerySegments(result, selectStatement);
        return result;
    }
    
    private static void extractSubquerySegments(final List<SubquerySegment> result, final SelectStatement selectStatement) {
        extractSubquerySegmentsFromProjections(result, selectStatement.getProjections());
        selectStatement.getFrom().ifPresent(optional -> extractSubquerySegmentsFromTableSegment(result, optional));
        if (selectStatement.getWhere().isPresent()) {
            extractSubquerySegmentsFromWhere(result, selectStatement.getWhere().get().getExpr());
        }
        if (selectStatement.getCombine().isPresent()) {
            extractSubquerySegmentsFromCombine(result, selectStatement.getCombine().get());
        }
        if (selectStatement.getWithSegment().isPresent()) {
            extractSubquerySegmentsFromCTEs(result, selectStatement.getWithSegment().get().getCommonTableExpressions());
        }
    }
    
    private static void extractSubquerySegmentsFromCTEs(final List<SubquerySegment> result, final Collection<CommonTableExpressionSegment> withSegment) {
        for (CommonTableExpressionSegment each : withSegment) {
            each.getSubquery().setSubqueryType(SubqueryType.WITH);
            result.add(each.getSubquery());
            extractSubquerySegments(result, each.getSubquery().getSelect());
        }
    }
    
    private static void extractSubquerySegmentsFromProjections(final List<SubquerySegment> result, final ProjectionsSegment projections) {
        if (null == projections || projections.getProjections().isEmpty()) {
            return;
        }
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                SubquerySegment subquery = ((SubqueryProjectionSegment) each).getSubquery();
                subquery.setSubqueryType(SubqueryType.PROJECTION);
                result.add(subquery);
                extractSubquerySegments(result, subquery.getSelect());
            } else if (each instanceof ExpressionProjectionSegment) {
                extractSubquerySegmentsFromExpression(result, ((ExpressionProjectionSegment) each).getExpr(), SubqueryType.PROJECTION);
            }
        }
    }
    
    private static void extractSubquerySegmentsFromTableSegment(final List<SubquerySegment> result, final TableSegment tableSegment) {
        if (tableSegment instanceof SubqueryTableSegment) {
            extractSubquerySegmentsFromSubqueryTableSegment(result, (SubqueryTableSegment) tableSegment);
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getLeft());
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getRight());
        }
    }
    
    private static void extractSubquerySegmentsFromJoinTableSegment(final List<SubquerySegment> result, final TableSegment tableSegment) {
        if (tableSegment instanceof SubqueryTableSegment) {
            SubquerySegment subquery = ((SubqueryTableSegment) tableSegment).getSubquery();
            subquery.setSubqueryType(SubqueryType.JOIN);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        } else if (tableSegment instanceof JoinTableSegment) {
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getLeft());
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getRight());
        }
    }
    
    private static void extractSubquerySegmentsFromSubqueryTableSegment(final List<SubquerySegment> result, final SubqueryTableSegment subqueryTableSegment) {
        SubquerySegment subquery = subqueryTableSegment.getSubquery();
        subquery.setSubqueryType(SubqueryType.TABLE);
        result.add(subquery);
        extractSubquerySegments(result, subquery.getSelect());
    }
    
    private static void extractSubquerySegmentsFromWhere(final List<SubquerySegment> result, final ExpressionSegment expressionSegment) {
        extractSubquerySegmentsFromExpression(result, expressionSegment, SubqueryType.PREDICATE);
    }
    
    private static void extractSubquerySegmentsFromExpression(final List<SubquerySegment> result, final ExpressionSegment expressionSegment, final SubqueryType subqueryType) {
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            SubquerySegment subquery = ((SubqueryExpressionSegment) expressionSegment).getSubquery();
            subquery.setSubqueryType(subqueryType);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
        if (expressionSegment instanceof ExistsSubqueryExpression) {
            SubquerySegment subquery = ((ExistsSubqueryExpression) expressionSegment).getSubquery();
            subquery.setSubqueryType(subqueryType);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
        if (expressionSegment instanceof ListExpression) {
            ((ListExpression) expressionSegment).getItems().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType));
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getLeft(), subqueryType);
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getRight(), subqueryType);
        }
        if (expressionSegment instanceof InExpression) {
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getLeft(), subqueryType);
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getRight(), subqueryType);
        }
        if (expressionSegment instanceof BetweenExpression) {
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getBetweenExpr(), subqueryType);
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getAndExpr(), subqueryType);
        }
        if (expressionSegment instanceof NotExpression) {
            extractSubquerySegmentsFromExpression(result, ((NotExpression) expressionSegment).getExpression(), subqueryType);
        }
        if (expressionSegment instanceof FunctionSegment) {
            ((FunctionSegment) expressionSegment).getParameters().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType));
        }
        if (expressionSegment instanceof MatchAgainstExpression) {
            extractSubquerySegmentsFromExpression(result, ((MatchAgainstExpression) expressionSegment).getExpr(), subqueryType);
        }
        if (expressionSegment instanceof CaseWhenExpression) {
            extractSubquerySegmentsFromCaseWhenExpression(result, (CaseWhenExpression) expressionSegment, subqueryType);
        }
        if (expressionSegment instanceof CollateExpression) {
            extractSubquerySegmentsFromExpression(result, ((CollateExpression) expressionSegment).getCollateName(), subqueryType);
        }
        if (expressionSegment instanceof DatetimeExpression) {
            extractSubquerySegmentsFromExpression(result, ((DatetimeExpression) expressionSegment).getLeft(), subqueryType);
            extractSubquerySegmentsFromExpression(result, ((DatetimeExpression) expressionSegment).getRight(), subqueryType);
        }
        if (expressionSegment instanceof NotExpression) {
            extractSubquerySegmentsFromExpression(result, ((NotExpression) expressionSegment).getExpression(), subqueryType);
        }
        if (expressionSegment instanceof TypeCastExpression) {
            extractSubquerySegmentsFromExpression(result, ((TypeCastExpression) expressionSegment).getExpression(), subqueryType);
        }
    }
    
    private static void extractSubquerySegmentsFromCaseWhenExpression(final List<SubquerySegment> result, final CaseWhenExpression expressionSegment, final SubqueryType subqueryType) {
        extractSubquerySegmentsFromExpression(result, expressionSegment.getCaseExpr(), subqueryType);
        expressionSegment.getWhenExprs().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType));
        expressionSegment.getThenExprs().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType));
        extractSubquerySegmentsFromExpression(result, expressionSegment.getElseExpr(), subqueryType);
    }
    
    private static void extractSubquerySegmentsFromCombine(final List<SubquerySegment> result, final CombineSegment combineSegment) {
        result.add(combineSegment.getLeft());
        result.add(combineSegment.getRight());
        extractSubquerySegments(result, combineSegment.getLeft().getSelect());
        extractSubquerySegments(result, combineSegment.getRight().getSelect());
    }
}
