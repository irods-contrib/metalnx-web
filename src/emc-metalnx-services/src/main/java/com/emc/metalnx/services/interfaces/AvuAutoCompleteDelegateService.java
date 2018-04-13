package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.datautils.avuautocomplete.AvuAutocompleteService.AvuTypeEnum;

public interface AvuAutoCompleteDelegateService {

	public String getMetadataAttrs(final String prefix, final int offset, final AvuTypeEnum avuTypeEnum)
			throws JargonException;
}
