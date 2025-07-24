/**
 * 
 */
package org.irodsext.dataprofiler;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerFactory;
import org.irods.jargon.extensions.dataprofiler.DataProfilerService;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionServiceFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.services.interfaces.UserService;

/**
 * Factory for {@link DataProfiler} implementation
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class IrodsextDataProfilerFactoryImpl implements DataProfilerFactory {

	public static final Logger log = LogManager.getLogger(IrodsextDataProfilerFactoryImpl.class);

	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private DataProfilerSettings dataProfilerSettings;

	private DataTypeResolutionServiceFactory dataTypeResolutionServiceFactory;

	/**
	 * MetaLnx service to map user/zone accounts to the MetaLnx {@link DataGridUser}
	 */
	private UserService userService;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.irodsext.dataprofiler.DataProfilerFactory#instanceDataProfilerService(org
	 * .irods.jargon.core.connection.IRODSAccount)
	 */
	@Override
	public DataProfilerService instanceDataProfilerService(final IRODSAccount irodsAccount) {
		validateContext();
		IrodsextDataProfilerService dataProfilerService = new IrodsextDataProfilerService(dataProfilerSettings,
				irodsAccessObjectFactory, irodsAccount);
		dataProfilerService.setDataTypeResolutionService(
				dataTypeResolutionServiceFactory.instanceDataTypeResolutionService(irodsAccount));
		dataProfilerService.setDataGridUser(resolveDataGridUser(irodsAccount));
		return dataProfilerService;
	}

	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

	public DataProfilerSettings getDataProfilerSettings() {
		return dataProfilerSettings;
	}

	public void setDataProfilerSettings(DataProfilerSettings dataProfilerSettings) {
		this.dataProfilerSettings = dataProfilerSettings;
	}

	public DataTypeResolutionServiceFactory getDataTypeResolutionServiceFactory() {
		return dataTypeResolutionServiceFactory;
	}

	public void setDataTypeResolutionServiceFactory(DataTypeResolutionServiceFactory dataTypeResolutionServiceFactory) {
		this.dataTypeResolutionServiceFactory = dataTypeResolutionServiceFactory;
	}

	/**
	 * Just a sanity check
	 */
	private void validateContext() {
		if (irodsAccessObjectFactory == null) {
			throw new JargonRuntimeException("null irodsAccessObjectFactory");
		}

		if (dataProfilerSettings == null) {
			throw new JargonRuntimeException("null dataProfilerSettings");
		}

		if (dataTypeResolutionServiceFactory == null) {
			throw new IllegalArgumentException("null dataTypeResolutionServiceFactory");
		}

	}

	public UserService getUserService() {
		return userService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	/*
	 * For some use cases, the {@code UserService} might not be provided. In this
	 * case, just use a stand in
	 */
	private DataGridUser resolveDataGridUser(final IRODSAccount irodsAccount) {
		log.info("resolveDataGridUser");

		if (userService == null) {
			log.debug("no user service provisioned, use a stand-in based on the iRODS account");
			DataGridUser dataGridUser = new DataGridUser();
			dataGridUser.setUsername(irodsAccount.getUserName());
			dataGridUser.setPassword(irodsAccount.getPassword());
			return dataGridUser;
		} else {
			return userService.findByUsernameAndZone(irodsAccount.getUserName(), irodsAccount.getZone());
		}

	}

	@Override
	public DataProfilerService instanceDataProfilerService(IRODSAccount irodsAccount,
			DataProfilerSettings overrideDataProfilerSettings) {
		validateContext();
		IrodsextDataProfilerService dataProfilerService = new IrodsextDataProfilerService(overrideDataProfilerSettings,
				irodsAccessObjectFactory, irodsAccount);
		dataProfilerService.setDataTypeResolutionService(
				dataTypeResolutionServiceFactory.instanceDataTypeResolutionService(irodsAccount));
		dataProfilerService.setDataGridUser(resolveDataGridUser(irodsAccount));
		return dataProfilerService;
	}

}
