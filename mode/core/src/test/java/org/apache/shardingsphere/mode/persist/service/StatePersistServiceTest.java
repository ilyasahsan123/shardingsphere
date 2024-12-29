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

package org.apache.shardingsphere.mode.persist.service;

import org.apache.shardingsphere.mode.persist.service.unified.StatePersistService;
import org.apache.shardingsphere.mode.state.ClusterState;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatePersistServiceTest {
    
    private StatePersistService statePersistService;
    
    @Mock
    private PersistRepository repository;
    
    @BeforeEach
    void setUp() {
        statePersistService = new StatePersistService(repository);
    }
    
    @Test
    void assertUpdate() {
        statePersistService.update(ClusterState.OK);
        verify(repository).persist("/states/cluster_state", ClusterState.OK.name());
    }
    
    @Test
    void assertLoad() {
        when(repository.query("/states/cluster_state")).thenReturn(ClusterState.READ_ONLY.name());
        assertThat(statePersistService.load(), is(ClusterState.READ_ONLY));
    }
    
    @Test
    void assertLoadWithEmptyState() {
        when(repository.query("/states/cluster_state")).thenReturn("");
        assertThat(statePersistService.load(), is(ClusterState.OK));
    }
}
