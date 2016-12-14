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

package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.core.domain.utils.DataGridCoreUtils;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.INTERFACES)
public class RuleServiceImpl implements RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleServiceImpl.class);
    private static final String POPULATE_RULE = "populateMetadataForFile";
    private static final String JPG_RULE = "automaticJpgMetadataExtraction";
    private static final String VCF_RULE = "automaticVcfMetadataExtraction";
    private static final String BAM_CRAM_RULE = "automaticBamMetadataExtraction";
    private static final String XML_MANIFEST_RULE = "automaticExtractMetadataFromXMLManifest";
    private static final String ILLUMINA_RULE = "illuminaMetadataForFile";
    private static final String REPL_DATA_OBJ_RULE = "replicateDataObjInAdminMode";

    // Maps rules for their respective microservices
    private static final Map<String, String> rulesMap;

    static {
        Map<String, String> map = new HashMap<>();
        map.put(POPULATE_RULE, "msiobjput_populate");
        map.put(JPG_RULE, "msiobjjpeg_extract");
        map.put(VCF_RULE, "msiobjput_mdvcf");
        map.put(BAM_CRAM_RULE, "msiobjput_mdbam");
        map.put(XML_MANIFEST_RULE, "msiobjput_mdmanifest");
        map.put(REPL_DATA_OBJ_RULE, "msiDataObjRepl");
        rulesMap = Collections.unmodifiableMap(map);
    }

    @Autowired
    CollectionService cs;

    @Autowired
    private IRODSServices is;

    @Autowired
    private ResourceService rs;

    @Value("${irods.host}")
    private String iCATHost;

    @Value("${populate.msi.enabled}")
    private boolean populateMsiEnabled;

    @Value("${illumina.msi.enabled}")
    private boolean illuminaMsiEnabled;

    public void execReplDataObjRule(String destResc, String path, boolean inAdminMode) throws DataGridRuleException, DataGridConnectionRefusedException {
        String flags = String.format("destRescName=%s%s", destResc, inAdminMode ? "++++irodsAdmin=" : "");
        executeRule(buildRule(destResc, REPL_DATA_OBJ_RULE, rulesMap.get(REPL_DATA_OBJ_RULE), path, flags, "null"));
    }

    public void execPopulateMetadataRule(String destResc, String objPath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!populateMsiEnabled) return;
        executeRule(buildRule(destResc, POPULATE_RULE, rulesMap.get(POPULATE_RULE), objPath));
    }

    public void execImageRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!DataGridCoreUtils.isImageFile(objPath)) return;
        executeRule(buildRule(destResc, JPG_RULE, rulesMap.get(JPG_RULE), objPath, filePath));
    }

    public void execVCFMetadataRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!DataGridCoreUtils.isVCFFile(objPath)) return;
        executeRule(buildRule(destResc, VCF_RULE, rulesMap.get(VCF_RULE), objPath, filePath));
    }

    public void execBamCramMetadataRule(String destResc, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!DataGridCoreUtils.isBamOrCram(objPath)) return;
        executeRule(buildRule(destResc, BAM_CRAM_RULE, rulesMap.get(BAM_CRAM_RULE), objPath, filePath));
    }

    public void execManifestFileRule(String destResc, String targetPath, String objPath, String filePath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!DataGridCoreUtils.isPrideXMLManifestFile(objPath)) return;

        String msiName = rulesMap.get(XML_MANIFEST_RULE);

        List<DataGridCollectionAndDataObject> objs = cs.getSubCollectionsAndDataObjetsUnderPath(targetPath);

        for (DataGridCollectionAndDataObject obj : objs) {
            logger.info("Extracting metadata from [{}] and applying on [{}]", filePath, objPath);

            String rule = buildRule(XML_MANIFEST_RULE, destResc, msiName, obj.getPath(), filePath, filePath);

            executeRule(rule);
        }
    }

    public void execIlluminaMetadataRule(String destResc, String targetPath, String objPath) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (!illuminaMsiEnabled || !objPath.endsWith("_SSU.tar")) return;

        RemoteRuleHeader header = new RemoteRuleHeader(destResc);

        String msiTarFileExtract = String.format("    %s(%s, *Status);\n", "msiTarFileExtract", escapeRuleParams(objPath, targetPath, destResc));
        String msiIllumina = String.format("    %s(%s);\n", "msiget_illumina_meta", escapeRuleParams(objPath, destResc));

        StringBuilder rule = new StringBuilder();
        rule.append("\n");
        rule.append(ILLUMINA_RULE);
        rule.append("{");
        rule.append("\n");
        rule.append(header.getRemoteRuleHeader());
        rule.append(msiTarFileExtract);
        rule.append(msiIllumina);
        rule.append(header.getRemoteRuleFooter());
        rule.append("}");
        rule.append("\n");
        rule.append("OUTPUT ruleExecOut\n");

        this.executeRule(rule.toString());
    }

    @Override
    public String buildRule(String resource, String ruleName, String msiName, String... params) throws DataGridConnectionRefusedException {
        RemoteRuleHeader header = new RemoteRuleHeader(resource);

        String msi = String.format("    %s(%s);\n", msiName, escapeRuleParams(params));

        StringBuilder rule = new StringBuilder();
        rule.append("\n");
        rule.append(ruleName);
        rule.append("{");
        rule.append("\n");
        rule.append(header.getRemoteRuleHeader());
        rule.append(msi);
        rule.append(header.getRemoteRuleFooter());
        rule.append("}");
        rule.append("\n");
        rule.append("OUTPUT ruleExecOut\n");

        return rule.toString();
    }

    @Override
    public void executeRule(String rule) throws DataGridRuleException, DataGridConnectionRefusedException {
        if (rule == null || rule.isEmpty()) return;

        RuleProcessingAO ruleProcessingAO = is.getRuleProcessingAO();

        try {
            ruleProcessingAO.executeRule(rule);
        } catch (JargonException e) {
            logger.error("Could not execute rule {}: {}.", rule, e.getMessage());
            throw new DataGridRuleException("Metadata extraction failed.");
        }
    }

    /**
     * Espaces all rule params to the format expected by the data grid
     * ex: msi_name("param1", "param2", "param3");
     *
     * @param params string params to be escaped
     * @return params escaped "param1", "param2", "param3" ... "paramN"
     */
    private String escapeRuleParams(String... params) {
        if (params == null) return "";

        StringBuilder paramsEscaped = new StringBuilder();

        for (int i = 0; i < params.length - 1; i++) paramsEscaped.append(String.format("\"%s\", ", params[i]));

        paramsEscaped.append(String.format("\"%s\"", params[params.length - 1]));

        return paramsEscaped.toString();
    }

    private class RemoteRuleHeader {
        private String remoteHeader = null;
        private String remoteFooter = null;

        public RemoteRuleHeader(String destResc) throws DataGridConnectionRefusedException {

            remoteHeader = "";
            remoteFooter = "";
            DataGridResource dgResc = rs.find(destResc);

            if (!iCATHost.startsWith(dgResc.getHost())) {
                String remoteHost = dgResc.getHost();
                remoteHeader = String.format("  remote(\"%s\", \"\") {\n", remoteHost);
                remoteFooter = "  }\n";
            }
        }

        public String getRemoteRuleHeader() {
            return this.remoteHeader;
        }

        public String getRemoteRuleFooter() {
            return this.remoteFooter;
        }
    }
}
