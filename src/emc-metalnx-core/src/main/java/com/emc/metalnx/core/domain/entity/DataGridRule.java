package com.emc.metalnx.core.domain.entity;

import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * iRODS Rule.
 */
public class DataGridRule {
    @Value("${irods.host}")
    private String iCATHost;

    private String[] inputRuleParams; // rule input parameters
    private String[] outputRuleParams; // rule output parameters
    private String host;
    private String rule;
    private boolean isAtLeastIRODS420;

    private static final String INPUT = "INPUT";
    private static final String OUTPUT = "OUTPUT";
    private static final String RULE_EXEC_OUT = "ruleExecOut";
    private static final String RULE_INPUT_NULL = "null";

    public static final String GET_VERSION_RULE = "getVersion";
    public static final String POPULATE_RULE = "populateMetadataForFile";
    public static final String JPG_RULE = "automaticJpgMetadataExtraction";
    public static final String VCF_RULE = "automaticVcfMetadataExtraction";
    public static final String BAM_CRAM_RULE = "automaticBamMetadataExtraction";
    public static final String XML_MANIFEST_RULE = "automaticExtractMetadataFromXMLManifest";
    public static final String REPL_DATA_OBJ_RULE = "replicateDataObjInAdminMode";
    public static final String ILLUMINA_RULE = "illuminaMetadataForFile";
    public static final String TAR_RULE = "extractTar";
    public static final String GET_MSIS_RULE = "getMicroservices";

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
        map.put(GET_VERSION_RULE, "msiobjget_version");
        map.put(ILLUMINA_RULE, "msiget_illumina_meta");
        map.put(TAR_RULE, "msiTarFileExtract");
        map.put(GET_MSIS_RULE, "msiobjget_microservices");
        rulesMap = Collections.unmodifiableMap(map);
    }

    public DataGridRule(String rule, String host, boolean isAtLeastIRODS420) {
        this.host = host;
        this.rule = rule;
        this.isAtLeastIRODS420 = isAtLeastIRODS420;
    }

    public DataGridRule(String rule, String host) {
        this(rule, host, true);
    }

    public void setInputRuleParams(String... params) { this.inputRuleParams = params; }

    public void setOutputRuleParams(String... params) { this.outputRuleParams = params; }

    public boolean isAtLeastIRODS420() {
        return isAtLeastIRODS420;
    }

    private String getInputParamsAsString() {
        StringBuilder sb = new StringBuilder();

        sb.append(INPUT);
        if(inputRuleParams != null) {
            String[] params = inputRuleParams;
            for (int i = 0; i < params.length - 1; i++) sb.append(String.format(" *p%d=\"%s\",", i, params[i]));
            sb.append(String.format(" *p%d=\"%s\"\n", params.length - 1, params[params.length - 1]));
        }
        else {
            sb.append(" ");
            sb.append(RULE_INPUT_NULL);
            sb.append("\n");
        }

        return sb.toString();
    }

    private String getOutputParamsAsString() {
        StringBuilder sb = new StringBuilder();

        sb.append(OUTPUT);

        if(outputRuleParams != null) {
            String[] params = outputRuleParams;
            for (int i = 0; i < params.length - 1; i++) sb.append(String.format(" *%s,", params[i]));
            sb.append(String.format(" *%s\n", params[params.length - 1]));
        }
        else {
            sb.append(" ");
            sb.append(RULE_EXEC_OUT);
            sb.append("\n");
        }

        return sb.toString();
    }

    private String getMSIParamsAsString() {
        StringBuilder paramsEscaped = new StringBuilder();
        paramsEscaped.append("    ");
        paramsEscaped.append(rulesMap.get(rule));
        paramsEscaped.append("(");

        if(inputRuleParams != null) {
            for (int i = 0; i < inputRuleParams.length - 1; i++) paramsEscaped.append(String.format("*p%d, ", i));
            paramsEscaped.append(String.format("*p%d", inputRuleParams.length - 1));

            if(outputRuleParams != null) paramsEscaped.append(", ");
        }

        if(outputRuleParams != null) {
            for (int i = 0; i < outputRuleParams.length - 1; i++) paramsEscaped.append(String.format("*%s, ", outputRuleParams[i]));
            paramsEscaped.append(String.format("*%s", outputRuleParams[outputRuleParams.length - 1]));
        }

        paramsEscaped.append(");\n");

        return paramsEscaped.toString();
    }

    private String declareOutputParams() {
        if(outputRuleParams == null) return "";

        StringBuilder outputParams = new StringBuilder();
        outputParams.append("  ");

        for (int i = 0; i < outputRuleParams.length - 1; i++) outputParams.append(String.format("*%s=\"\",", outputRuleParams[i]));
        outputParams.append(String.format("*%s=\"\";", outputRuleParams[outputRuleParams.length - 1]));

        return outputParams.toString();
    }

    public String toString() {
        RemoteRuleHeader header = new RemoteRuleHeader(host);
        StringBuilder ruleString = new StringBuilder();
        ruleString.append("\n");
        ruleString.append(rule);
        ruleString.append("{");
        ruleString.append("\n");
        if(!isAtLeastIRODS420) ruleString.append(declareOutputParams() + "\n");
        ruleString.append(header.getRemoteRuleHeader());
        ruleString.append(getMSIParamsAsString());
        ruleString.append(header.getRemoteRuleFooter());
        ruleString.append("}");
        ruleString.append("\n");
        ruleString.append(getInputParamsAsString());
        ruleString.append(getOutputParamsAsString());

        return ruleString.toString();
    }

    private class RemoteRuleHeader {
        private String remoteHeader = null;
        private String remoteFooter = null;

        RemoteRuleHeader(String host) {
            remoteHeader = String.format("  remote(\"%s\", \"\") {\n", host);
            remoteFooter = "  }\n";
        }

        String getRemoteRuleHeader() {
            return this.remoteHeader;
        }

        String getRemoteRuleFooter() {
            return this.remoteFooter;
        }
    }
}
