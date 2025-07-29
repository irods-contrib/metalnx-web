/**
 * 
 */
package org.irodsext.dataprofiler;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.JargonRuntimeException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.domain.Collection;
import org.irods.jargon.core.pub.domain.DataObject;
import org.irods.jargon.extensions.dataprofiler.DataProfile;
import org.irods.jargon.extensions.dataprofiler.DataProfilerService;
import org.irods.jargon.extensions.dataprofiler.DataProfilerSettings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emc.metalnx.core.domain.entity.DataGridUser;

/**
 * IRODS-EXT base implementation of a data profiler that can summarize a data
 * object or collection
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class IrodsextDataProfilerService extends DataProfilerService {

	/**
	 * logged in user identity
	 */
	private DataGridUser dataGridUser;

	public static final Logger log = LogManager.getLogger(IrodsextDataProfilerService.class);

	public IrodsextDataProfilerService(DataProfilerSettings defaultDataProfilerSettings,
			IRODSAccessObjectFactory irodsAccessObjectFactory, IRODSAccount irodsAccount) {
		super(defaultDataProfilerSettings, irodsAccessObjectFactory, irodsAccount);
	}

	@Override
	protected void addStarringDataToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
	}

	@Override
	protected void addStarringDataToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
	}

	@Override
	protected void addTaggingAndCommentsToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("tagging not yet implemented");

	}

	@Override
	protected void addTaggingAndCommentsToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("tagging not yet implemented");

	}

	@Override
	protected void addSharingToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("sharing not yet implemented");

	}

	@Override
	protected void addSharingToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("sharing not yet implemented");

	}

	@Override
	protected void addTicketsToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("tickets not yet implemented");

	}

	@Override
	protected void addTicketsToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("tickets not yet implemented");

	}

	@Override
	protected void addMetadataTemplatesToDataObject(DataProfile<DataObject> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("templates not yet implemented");

	}

	@Override
	protected void addMetadataTemplatesToCollection(DataProfile<Collection> dataProfile,
			DataProfilerSettings dataProfilerSettings) throws JargonException {
		log.warn("templates not yet implemented");

	}


	public DataGridUser getDataGridUser() {
		return dataGridUser;
	}

	public void setDataGridUser(DataGridUser dataGridUser) {
		this.dataGridUser = dataGridUser;
	}

}
