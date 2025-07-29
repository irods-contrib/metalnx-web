/**
 * 
 */
package org.irodsext.datatyper;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionService;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionServiceFactory;
import org.irods.jargon.extensions.datatyper.DataTyperSettings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Factory for {@link IrodsextDataTypeResolutionService}
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class IrodsextDataTypeResolutionServiceFactoryImpl implements DataTypeResolutionServiceFactory {

	@Autowired
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	@Autowired
	private DataTyperSettings dataTyperSettings;

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public DataTyperSettings getDataTyperSettings() {
		return dataTyperSettings;
	}

	public void setDataTyperSettings(DataTyperSettings dataTyperSettings) {
		this.dataTyperSettings = dataTyperSettings;
	}

	/* (non-Javadoc)
	 * @see org.irodsext.datatyper.DataTypeResolutionServiceFactory#instanceDataTypeResolutionService(org.irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataTypeResolutionService instanceDataTypeResolutionService(final IRODSAccount irodsAccount) {
		validateDependencies();
		DataTypeResolutionService dataTypeResolutionService = new IrodsextDataTypeResolutionService(
				irodsAccessObjectFactory, irodsAccount, dataTyperSettings);
		return dataTypeResolutionService;
	}

	/**
	 * Just a sanity check
	 */
	private void validateDependencies() {
		if (irodsAccessObjectFactory == null) {
			throw new JargonRuntimeException("null irodsAccessObjectFactory");
		}

		if (dataTyperSettings == null) {
			throw new JargonRuntimeException("null dataTyperSettings");
		}
	}

}
