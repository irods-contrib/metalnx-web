package com.emc.metalnx.services.irods;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;
import org.irods.jargon.datautils.avuautocomplete.AvuSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.AvuAutoCompleteDelegateService;
import com.emc.metalnx.services.interfaces.IRODSServices;

@Service
@Transactional
public class AvuAutoCompleteDelegateServiceImpl implements AvuAutoCompleteDelegateService {

	@Autowired
	private IRODSServices irodsServices;

	
	private static final Logger logger = LoggerFactory.getLogger(AvuAutoCompleteDelegateServiceImpl.class);

	@Override
	public AvuSearchResult getAvuAttrs() {
		try {
			AvuSearchResult result = new AvuSearchResult();
			logger.info("AvuAutoCompleteDelegateServiceImpl: getAvuAttrs()");
			AvuAutocompleteService autocompleteService = irodsServices.getAvuAutocompleteService();
			result = autocompleteService.gatherAvailableAttributes("%", 0, AvuTypeEnum.COLLECTION);
			logger.info("Result: {}", result);
			return result;
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
