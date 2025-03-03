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

package org.apache.shardingsphere.mode.node.rule.tuple;

import com.google.common.base.CaseFormat;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.yaml.config.pojo.rule.YamlRuleConfiguration;
import org.apache.shardingsphere.mode.node.rule.tuple.annotation.RuleRepositoryTupleField;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

/**
 * YAML rule configuration field utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlRuleConfigurationFieldUtil {
    
    /**
     * Get fields from YAML rule configuration.
     *
     * @param yamlRuleConfigurationClass YAML rule configuration class
     * @return got fields
     */
    public static Collection<Field> getFields(final Class<? extends YamlRuleConfiguration> yamlRuleConfigurationClass) {
        return Arrays.stream(yamlRuleConfigurationClass.getDeclaredFields())
                .filter(each -> null != each.getAnnotation(RuleRepositoryTupleField.class))
                .sorted(Comparator.comparingInt(o -> o.getAnnotation(RuleRepositoryTupleField.class).type().ordinal())).collect(Collectors.toList());
    }
    
    /**
     * Get tuple item name.
     *
     * @param field field from YAML rule configuration
     * @return tuple item name
     */
    public static String getTupleItemName(final Field field) {
        return CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
    }
}
