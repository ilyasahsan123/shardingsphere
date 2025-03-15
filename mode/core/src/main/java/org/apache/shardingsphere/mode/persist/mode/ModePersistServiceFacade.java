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

package org.apache.shardingsphere.mode.persist.mode;

import org.apache.shardingsphere.mode.persist.service.ComputeNodePersistService;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.persist.service.ProcessPersistService;

/**
 * Mode persist service facade.
 */
public interface ModePersistServiceFacade extends AutoCloseable {
    
    /**
     * Get meta data manager persist service.
     *
     * @return meta data manager persist service
     */
    MetaDataManagerPersistService getMetaDataManagerPersistService();
    
    /**
     * Get compute node persist service.
     *
     * @return compute node persist service
     */
    ComputeNodePersistService getComputeNodePersistService();
    
    /**
     * Get process persist service.
     *
     * @return process persist service
     */
    ProcessPersistService getProcessPersistService();
    
    @Override
    void close();
}
