/**
 * 
 */
package org.irodsext.datatyper;

import java.io.IOException;

import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.utils.LocalFileUtils;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.extensions.datatyper.DataType;
import org.irods.jargon.extensions.datatyper.DataTypeResolutionService;
import org.irods.jargon.extensions.datatyper.DataTyperSettings;
import org.irods.jargon.extensions.datatyper.IrodsMimeTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Data type resolution service to determine MIME and info types of a file. Note
 * that this is at first a very basic service that will need to evolve over
 * time.
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class IrodsextDataTypeResolutionService extends DataTypeResolutionService {

	public static final Logger log = LogManager.getLogger(IrodsextDataTypeResolutionService.class);

	public IrodsextDataTypeResolutionService(IRODSAccessObjectFactory irodsAccessObjectFactory,
			IRODSAccount irodsAccount, DataTyperSettings dataTyperSettings) {
		super(irodsAccessObjectFactory, irodsAccount, dataTyperSettings);
	}

	@Override
	public DataType resolveDataType(String irodsAbsolutePath) throws DataNotFoundException, JargonException {
		log.info("resolveDataType()");

		return resolveDataType(irodsAbsolutePath, this.getDefaultDataTyperSettings());

	}

	private String determineMimeTypeViaTika(String irodsAbsolutePath) throws JargonException {
		AutoDetectParser parser = new AutoDetectParser();
		Detector detector = parser.getDetector();
		Metadata md = new Metadata();
		String fileName = MiscIRODSUtils.getLastPathComponentForGivenAbsolutePath(irodsAbsolutePath);

		md.add(Metadata.RESOURCE_NAME_KEY, fileName);
		MediaType mediaType;
		try {
			mediaType = detector.detect(null, md);
		} catch (IOException e) {
			throw new JargonException("io exception determining file type by extension", e);
		}
		return mediaType.toString();
	}

	@Override
	public DataType resolveDataType(String irodsAbsolutePath, DataTyperSettings dataTyperSettings)
			throws DataNotFoundException, JargonException {
		log.info("resolveDataType()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		if (dataTyperSettings.isDetailedDetermination()) {
			log.warn("detailedDetermination not yet implemented, will default to check of file path");
		}

		log.info("checking for known irods types - interim code...");

		String mimeType = determimeMimeTypeOfIrodsObjects(irodsAbsolutePath);

		log.info("use Tika to derive based on file extenstion");

		if (mimeType == null) {
			log.info("not a known irods type, try tika");
			mimeType = determineMimeTypeViaTika(irodsAbsolutePath);
		}

		if (mimeType == null) {
			log.info("no mime type found via tika");
			mimeType = "";
		}

		DataType dataType = new DataType();
		dataType.setMimeType(mimeType);
		log.info("dataType:{}", dataType);
		return dataType;
	}

	/**
	 * front-load detection of special irods file types
	 * 
	 * @param dataObject
	 * @return {@link String} with the mime type of special irods objects
	 */
	private String determimeMimeTypeOfIrodsObjects(final String irodsAbsolutePath) {

		String extension = LocalFileUtils.getFileExtension(irodsAbsolutePath);
		if (extension == null || extension.isEmpty()) {
			return null;
		}

		if (extension.equals(".r")) {
			log.info("irods rule detected in:{}", irodsAbsolutePath);
			return IrodsMimeTypes.APPLICATION_IRODS_RULE;
		} else {
			return null;
		}

	}

	@Override
	public String quickMimeType(String irodsAbsolutePath) throws DataNotFoundException, JargonException {
		log.info("quickMimeType()");

		if (irodsAbsolutePath == null || irodsAbsolutePath.isEmpty()) {
			throw new IllegalArgumentException("null or empty irodsAbsolutePath");
		}

		log.info("irodsAbsolutePath:{}", irodsAbsolutePath);

		String mimeType = determimeMimeTypeOfIrodsObjects(irodsAbsolutePath);

		log.info("use Tika to derive based on file extenstion");

		if (mimeType == null) {
			log.info("not a known irods type, try tika");
			mimeType = determineMimeTypeViaTika(irodsAbsolutePath);
		}

		if (mimeType == null) {
			log.info("no mime type found via tika");
			mimeType = "";
		}

		log.info("mime type is:{}", mimeType);
		return mimeType;

	}

}
