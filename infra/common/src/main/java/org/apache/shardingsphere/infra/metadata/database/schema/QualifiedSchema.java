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

package org.apache.shardingsphere.infra.metadata.database.schema;

import lombok.EqualsAndHashCode;
import org.apache.shardingsphere.infra.metadata.identifier.ShardingSphereIdentifier;

/**
 * Qualified schema.
 */
@EqualsAndHashCode
public final class QualifiedSchema {
    
    private final ShardingSphereIdentifier databaseName;
    
    private final ShardingSphereIdentifier schemaName;
    
    public QualifiedSchema(final String schemaName) {
        this(null, schemaName);
    }
    
    public QualifiedSchema(final String databaseName, final String schemaName) {
        this.databaseName = new ShardingSphereIdentifier(databaseName);
        this.schemaName = new ShardingSphereIdentifier(schemaName);
    }
    
    /**
     * Get database name.
     *
     * @return database name
     */
    public String getDatabaseName() {
        return databaseName.getValue();
    }
    
    /**
     * Get schema name.
     *
     * @return schema name
     */
    public String getSchemaName() {
        return schemaName.getValue();
    }
    
    @Override
    public String toString() {
        return null == getDatabaseName() ? getSchemaName() : String.join(".", getDatabaseName(), getSchemaName());
    }
}
