package com.emc.metalnx.services.irods;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;
import org.irods.jargon.datautils.avuautocomplete.AvuSearchResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.AvuAutoCompleteDelegateService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class AvuAutoCompleteDelegateServiceImpl implements AvuAutoCompleteDelegateService {

	@Autowired
	private IRODSServices irodsServices;

	private static final Logger logger = LogManager.getLogger(AvuAutoCompleteDelegateServiceImpl.class);

	@Override
	public String getMetadataAttrs(final String prefix, final int offset, final AvuTypeEnum avuTypeEnum) throws JargonException {

		logger.info("getMetadataAttrs()");
		logger.info("prefix: {}", prefix);
		logger.info("offset: {}", offset);
		logger.info("avuTypeEnum: {}", avuTypeEnum);

		String jsonInString = "";

		try {
			AvuSearchResult result = new AvuSearchResult();			
			AvuAutocompleteService autocompleteService = irodsServices.getAvuAutocompleteService();
			result = autocompleteService.gatherAvailableAttributes(prefix, offset, avuTypeEnum);
			logger.info("Result from jargon: {}", result);

			//MetadataAttribForm jsonRes = new MetadataAttribForm();
			//BeanUtils.copyProperties(result, jsonRes);
			//logger.info("jargon result obj copied to jsonRes: {}", jsonRes);

			// java pojo to json
			ObjectMapper mapper = new ObjectMapper();
			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
			logger.info("java pojo to jsonInString: {}", jsonInString);

		} catch (JargonException e) {			
			throw e;			
		}
		catch (JsonProcessingException e) {
			logger.error("JsonProsessingException: ", e.getMessage());
		}
		return jsonInString;
	}

	@Override
	public String getAvailableValues(String forAttribute, String prefix, int offset, AvuTypeEnum avuTypeEnum)
			throws JargonException {
		
		logger.info("getMetadataAttrs()");
		logger.info("prefix: {}", prefix);
		logger.info("offset: {}", offset);
		logger.info("avuTypeEnum: {}", avuTypeEnum);

		String jsonInString = "";

		try {
			AvuSearchResult result = new AvuSearchResult();			
			AvuAutocompleteService autocompleteService = irodsServices.getAvuAutocompleteService();
			result = autocompleteService.gatherAvailableValues(forAttribute, prefix, offset, avuTypeEnum);
			logger.info("Result from jargon: {}", result);

			//MetadataAttribForm jsonRes = new MetadataAttribForm();
			//BeanUtils.copyProperties(result, jsonRes);
			//logger.info("jargon result obj copied to jsonRes: {}", jsonRes);

			// java pojo to json
			ObjectMapper mapper = new ObjectMapper();
			jsonInString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
			logger.info("java pojo to jsonInString: {}", jsonInString);

		} catch (JargonException e) {			
			throw e;			
		}
		catch (JsonProcessingException e) {
			logger.error("JsonProsessingException: ", e.getMessage());
		}
		return jsonInString;
	}

}
