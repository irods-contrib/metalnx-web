package com.emc.metalnx.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IRODSRuleParameter;
import org.irods.jargon.core.rule.IrodsRuleInvocationTypeEnum;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Backend API support for search operations
 * 
 * @author Mike Conway - NIEHS
 *
 */

@Controller
//@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/api/gallery")
public class GalleryApiController {

    public static final Logger log = LogManager.getLogger(GalleryApiController.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    IRODSServices irodsServices;

    @Value("${gallery_view.rule_engine_plugin.instance_name}")
    private String ruleEnginePluginInstanceName;

    public IRODSServices getIrodsServices() {
        return irodsServices;
    }

    public void setIrodsServices(IRODSServices irodsServices) {
        this.irodsServices = irodsServices;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String list(@RequestParam("path") String path,
                       @RequestParam("offset") int offset,
                       @RequestParam("limit") int limit,
                       @RequestParam("policyComposed") Optional<Boolean> policyComposed)
        throws JargonException
    {
        log.info("list()");
        log.info("path:{}", path);
        log.info("offset:{}", offset);
        log.info("limit:{}", limit);
        log.info("policyComposed:{}", policyComposed);

        // TODO This function returns an error object as JSON when the rule
        // encounters execution issues. Exceptions are thrown if invalid inputs
        // are provided or missing.
        //
        // Q. How are callers expected to deal with multiple ways of failing?
        // Q. Shouldn't there be only one true way to report errors?

        if (null == path || path.isEmpty()) {
            throw new IllegalArgumentException("null or empty logical path");
        }

        if (offset < 0) {
            throw new IllegalArgumentException("offset is less than zero");
        }

        if (limit < 0) {
            throw new IllegalArgumentException("limit is less than zero");
        }

        if (policyComposed.orElse(false)) {
            return listThumbnailInfoPolicyComposed(path, offset, limit);
        }
        
        return listThumbnailInfo(path, offset, limit);
    }
    
    private String listThumbnailInfo(String path, int offset, int limit)
        throws DataGridConnectionRefusedException, JargonException
    {
        IRODSAccessObjectFactory aof = irodsServices.getIrodsAccessObjectFactory();

        // TODO The rule name could be configurable as well.
        StringBuilder sb = new StringBuilder();
        sb.append("gallery_view { irods_policy_list_thumbnails_for_logical_path(*path, *offset, *limit, *out); }\n");
        sb.append("INPUT *path=,*offset=,*limit=\n");
        sb.append("OUTPUT *out");

        List<IRODSRuleParameter> params = new ArrayList<>();
        params.add(new IRODSRuleParameter("*path", path));
        params.add(new IRODSRuleParameter("*offset", String.valueOf(offset)));
        params.add(new IRODSRuleParameter("*limit", String.valueOf(limit)));

        // TODO There is probably a better way to get the underlying iRODS account.
        RuleProcessingAO rpao = aof.getRuleProcessingAO(irodsServices.getCollectionAO().getIRODSAccount());
        RuleInvocationConfiguration ctx = new RuleInvocationConfiguration();
        ctx.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.OTHER);
        ctx.setRuleEngineSpecifier(ruleEnginePluginInstanceName);

        try {
            // This will throw if the rule does not exist.
            IRODSRuleExecResult execRes = rpao.executeRule(sb.toString(), params, ctx);
            IRODSRuleExecResultOutputParameter outParam = execRes.getOutputParameterResults().get("*out");

            if (null == outParam) {
                log.error("*out parameter not found in rule execution results");
                return newErrorObjectAsJson();
            }

            return (String) outParam.getResultObject();
        }
        catch (JargonException e) {
            log.error("error executing rule", e);
            return newErrorObjectAsJson();
        }
    }

    private String listThumbnailInfoPolicyComposed(String _path, int _offset, int _limit)
        throws DataGridConnectionRefusedException, JargonException
    {
        IRODSAccessObjectFactory aof = irodsServices.getIrodsAccessObjectFactory();
        
        // TODO The rule name could be configurable as well.
        StringBuilder sb = new StringBuilder();
        sb.append("gallery_view { irods_policy_composed_list_thumbnails_for_logical_path(*params, *config, *out); }\n");
        sb.append("INPUT *params=,*config=\n");
        sb.append("OUTPUT *out");

        List<IRODSRuleParameter> params = new ArrayList<>();

        try {
            class JsonParams
            {
                @JsonProperty("logical_path") String logicalPath = _path;
                @JsonProperty("offset") int offset = _offset;
                @JsonProperty("limit") int limit = _limit;
            }

            Map<String, String> jsonConfig = new HashMap<>();
            jsonConfig.put("version", "4.2.9");
            jsonConfig.put("zone_hint", "tempZone");

            params.add(new IRODSRuleParameter("*params", objectMapper.writeValueAsString(new JsonParams())));
            params.add(new IRODSRuleParameter("*config", objectMapper.writeValueAsString(jsonConfig)));
        }
        catch (JsonProcessingException e) {
            throw new JargonException("unable to serialize error object", e);
        }

        // TODO There is probably a better way to get the underlying iRODS account.
        RuleProcessingAO rpao = aof.getRuleProcessingAO(irodsServices.getCollectionAO().getIRODSAccount());
        RuleInvocationConfiguration ctx = new RuleInvocationConfiguration();
        ctx.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.OTHER);
        ctx.setRuleEngineSpecifier(ruleEnginePluginInstanceName);

        try {
            // This will throw if the rule does not exist.
            IRODSRuleExecResult execRes = rpao.executeRule(sb.toString(), params, ctx);
            IRODSRuleExecResultOutputParameter outParam = execRes.getOutputParameterResults().get("*out");

            if (null == outParam) {
                log.error("*out parameter not found in rule execution results");
                return newErrorObjectAsJson();
            }

            return (String) outParam.getResultObject();
        }
        catch (JargonException e) {
            log.error("error executing rule", e);
            return newErrorObjectAsJson();
        }
    }
    
    private String newErrorObjectAsJson() throws JargonException
    {
        // The presence of this property is an indicator to the caller that
        // something went wrong. This property will not exist in rules that
        // were executed successfully.
        class Error { @JsonProperty("error") final boolean v = true; }

        try {
            return objectMapper.writeValueAsString(new Error());
        }
        catch (JsonProcessingException e) {
            throw new JargonException("unable to serialize error object", e);
        }
    }

}
