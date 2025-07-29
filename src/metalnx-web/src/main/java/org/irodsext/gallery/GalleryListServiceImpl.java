/**
 * 
 */
package org.irodsext.gallery;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.RuleProcessingAO;
import org.irods.jargon.core.rule.IRODSRuleExecResult;
import org.irods.jargon.core.rule.IRODSRuleExecResultOutputParameter;
import org.irods.jargon.core.rule.IrodsRuleInvocationTypeEnum;
import org.irods.jargon.core.rule.RuleInvocationConfiguration;
import org.irods.jargon.core.service.AbstractJargonService;
import org.irods.jargon.extensions.thumbnail.GalleryListService;
import org.irods.jargon.extensions.thumbnail.ThumbnailList;
import org.irodsext.dataprofiler.IrodsextDataProfilerService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author conwaymc
 *
 */
public class GalleryListServiceImpl extends AbstractJargonService implements GalleryListService {

	public static final Logger log = LogManager.getLogger(IrodsextDataProfilerService.class);
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * Default (no values) constructor
	 */
	public GalleryListServiceImpl() {
		super();
	}

	/**
	 * @param irodsAccessObjectFactory {@link IRODSAccessObjectFactory}
	 * @param irodsAccount             {@link IRODSAccount}
	 */
	public GalleryListServiceImpl(IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		super(irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	public ThumbnailList list(String irodsFileAbsolutePath, int offset, int length) throws JargonException {
		log.info("list()");

		if (irodsFileAbsolutePath == null || irodsFileAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsFileAbsolutePath");
		}

		if (offset < 0) {
			throw new IllegalArgumentException("offset cannot be < 0");
		}

		if (length <= 0) {
			throw new IllegalArgumentException("length cannot be <= 0");
		}

		log.info("irodsFileAbsolutePath:{}", irodsFileAbsolutePath);
		log.info("offset:{}", offset);
		log.info("length:{}", length);

		RuleInvocationConfiguration ruleInvocationConfiguration = RuleInvocationConfiguration
				.instanceWithDefaultAutoSettings();
		ruleInvocationConfiguration.setIrodsRuleInvocationTypeEnum(IrodsRuleInvocationTypeEnum.IRODS);

		StringBuilder sb = new StringBuilder();
		sb.append("myrule { irods_policy_list_thumbnails_for_logical_path(\"");
		sb.append(irodsFileAbsolutePath);
		sb.append("\",\"");
		sb.append(offset);
		sb.append("\",\"");
		sb.append(length);
		sb.append("\",*out) }");
		sb.append("\nINPUT null");
		sb.append("\nOUTPUT *out");

		/*
		 * List<IRODSRuleParameter> inputParameters = new ArrayList<>();
		 * inputParameters.add(new IRODSRuleParameter("*logical_path",
		 * irodsFileAbsolutePath)); inputParameters.add(new
		 * IRODSRuleParameter("*offset", String.valueOf(offset)));
		 * inputParameters.add(new IRODSRuleParameter("*limit",
		 * String.valueOf(length)));
		 */

		RuleProcessingAO ruleProcessingAO = this.getIrodsAccessObjectFactory().getRuleProcessingAO(getIrodsAccount());
		/*
		 * IRODSRuleExecResult result =
		 * ruleProcessingAO.executeRuleFromResource("/rules/call_gallery_list.r", null,
		 * ruleInvocationConfiguration);
		 */

		log.info("ruleString:{}", sb.toString());

		IRODSRuleExecResult result = ruleProcessingAO.executeRule(sb.toString(), null, ruleInvocationConfiguration);

		log.debug("result:{}", result);
		IRODSRuleExecResultOutputParameter outParam = result.getOutputParameterResults().get("*out");

		// I expect an out param from the rule
		if (outParam == null) {
			log.error("out param missing in rule{}", result);
			throw new JargonRuntimeException("unexpected result from rule call");
		}

		String galleryData = (String) outParam.getResultObject();

		try {
			ThumbnailList thumbnailListEntry = objectMapper.readerFor(ThumbnailList.class).readValue(galleryData);
			return thumbnailListEntry;
		} catch (JsonProcessingException e) {
			log.error("error parsing thumbnail response", e);
			throw new JargonException("Invalid listing response", e);
		}

	}

}
