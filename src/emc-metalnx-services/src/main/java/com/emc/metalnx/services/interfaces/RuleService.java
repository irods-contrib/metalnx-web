/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;

import java.util.List;
import java.util.Map;

public interface RuleService {

    /**
     * Executes the remove collection microservice.
     * @param destResc resource where the data object is
     * @param objPath trash path to be emptied
     * @param inAdminMode execute empty trash in admin mode or not
     * @throws DataGridConnectionRefusedException if there is no connection to the grid
     * @throws DataGridRuleException if an error happens during the rule execution
     */
    void execEmptyTrashRule(String destResc, String objPath, boolean inAdminMode) throws DataGridConnectionRefusedException, DataGridRuleException;

    /**
     * Executes the get microservices MSI.
     * @param host server's hostname
     * @return List of MSIs on the server that resource is.
     */
    List<String> execGetMSIsRule(String host) throws DataGridConnectionRefusedException, DataGridRuleException;

    /**
     * Executes the get version MSI.
     * @param host server's hostname
     * @return version of the MSI currently installed
     * @throws DataGridRuleException if an error happens during the rule execution
     * @throws DataGridConnectionRefusedException if there is no connection to the grid
     */
    String execGetVersionRule(String host) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Executes the replicate data object rule
     *
     * @param destResc    resource where the data object is
     * @param path        path to the data object
     * @param inAdminMode True, if the replication has to run as admin. False, otherwise.
     * @throws DataGridRuleException              if replication fails.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid if Metalnx cannot connect to the data grid.
     */
    void execReplDataObjRule(String destResc, String path, boolean inAdminMode) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute populate metadata rule.
     *
     * @param host hostname of the machine to run the rule
     * @param objPath  path to the object in the data grid path to the object in the data grid
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execPopulateMetadataRule(String host, String objPath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from image files rule.
     *
     * @param host hostname of the machine to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid if Metalnx cannot connect to the data grid
     */
    void execImageRule(String host, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from VCF files rule.
     *
     * @param host hostname of the machine to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execVCFMetadataRule(String host, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from BAM or CRAM files rule.
     *
     * @param host hostname of the machine to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execBamCramMetadataRule(String host, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from manifest files rule.
     *
     * @param host hostname of the machine to run the rule
     * @param targetPath target path target path
     * @param objPath    path to the object in the data grid
     * @param filePath   physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execManifestFileRule(String host, String targetPath, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from Illumina files rule.
     *
     * @param destResc   resource to run the rule
     * @param targetPath target path
     * @param objPath    path to the object in the data grid
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execIlluminaMetadataRule(String destResc, String targetPath, String objPath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Executes a rule in the data grid
     *
     * @param rule rule string to be executed
     * @throws DataGridRuleException if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    Map<String, IRODSRuleExecResultOutputParameter> executeRule(String rule) throws DataGridRuleException, DataGridConnectionRefusedException;
}
