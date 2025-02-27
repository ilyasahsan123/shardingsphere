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

package org.apache.shardingsphere.mode.manager.cluster.fixture;

import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapper;

public final class YamlRuleConfigurationSwapperFixture implements YamlRuleConfigurationSwapper<YamlRuleConfigurationFixture, RuleConfigurationFixture> {
    
    @Override
    public Class<RuleConfigurationFixture> getTypeClass() {
        return RuleConfigurationFixture.class;
    }
    
    @Override
    public YamlRuleConfigurationFixture swapToYamlConfiguration(final RuleConfigurationFixture data) {
        YamlRuleConfigurationFixture result = new YamlRuleConfigurationFixture();
        result.setUnique(data.getUnique());
        result.setNamed(data.getNamed());
        return result;
    }
    
    @Override
    public RuleConfigurationFixture swapToObject(final YamlRuleConfigurationFixture yamlConfig) {
        RuleConfigurationFixture result = new RuleConfigurationFixture();
        result.setUnique(yamlConfig.getUnique());
        result.setNamed(yamlConfig.getNamed());
        return result;
    }
    
    @Override
    public String getRuleTagName() {
        return "FIXTURE";
    }
    
    @Override
    public int getOrder() {
        return 3;
    }
}
