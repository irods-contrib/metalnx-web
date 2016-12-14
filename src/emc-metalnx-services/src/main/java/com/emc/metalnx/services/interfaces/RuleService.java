/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;

public interface RuleService {

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
     * @param destResc resource to run the rule
     * @param objPath  path to the object in the data grid path to the object in the data grid
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execPopulateMetadataRule(String destResc, String objPath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from image files rule.
     *
     * @param destResc resource to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid if Metalnx cannot connect to the data grid
     */
    void execImageRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from VCF files rule.
     *
     * @param destResc resource to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execVCFMetadataRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from BAM or CRAM files rule.
     *
     * @param destResc resource to run the rule
     * @param objPath  path to the object in the data grid
     * @param filePath physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execBamCramMetadataRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

    /**
     * Execute metadata extraction from manifest files rule.
     *
     * @param destResc   resource to run the rule
     * @param targetPath target path target path
     * @param objPath    path to the object in the data grid
     * @param filePath   physical file path
     * @throws DataGridRuleException              if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void execManifestFileRule(String destResc, String targetPath, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException;

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
     * Builds a rule in the data grid
     *
     * @param resource resource name where this rule will be executed
     * @param ruleName name of the rule that will be executed
     * @param msiName  name of the microservice that will be called within the rule
     * @param params   all parameters of the msi called by the rule
     * @return String representing the rule already formatted properly to be executed
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    String buildRule(String resource, String ruleName, String msiName, String... params) throws DataGridConnectionRefusedException;

    /**
     * Executes a rule in the data grid
     *
     * @param rule rule string to be executed
     * @throws DataGridRuleException if rule exection failed.
     * @throws DataGridConnectionRefusedException if Metalnx cannot connect to the data grid
     */
    void executeRule(String rule) throws DataGridRuleException, DataGridConnectionRefusedException;
}
