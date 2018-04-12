package com.emc.metalnx.services.irods;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;
import org.irods.jargon.datautils.avuautocomplete.AvuSearchResult;
import org.irods.jargon.metadatatemplate.MetadataTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.services.interfaces.AvuAutoCompleteDelegateService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.irods.utils.MetadataAttribForm;

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
			
			MetadataAttribForm jsonRes = new MetadataAttribForm();
			BeanUtils.copyProperties(result, jsonRes);
				
			logger.info("jsonRes: {}",jsonRes);
			/*// Create a JaxBContext
			JAXBContext jc = JAXBContext.newInstance(MetadataAttribForm.class);
			
			// Create the Marshaller Object using the JaxB Context
	        Marshaller marshaller = jc.createMarshaller();

			// marshall java object to JSON
			marshaller.marshal(result, System.out);*/
			return result;
		} catch (JargonException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
