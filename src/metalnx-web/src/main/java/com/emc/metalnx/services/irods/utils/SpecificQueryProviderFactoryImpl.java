/**
 * 
 */
package com.emc.metalnx.services.irods.utils;

import org.irods.jargon.core.protovalues.IcatTypeEnum;
import org.irods.jargon.core.pub.domain.ClientHints;

import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;

/**
 * Create the appropriate SpecificQueryProvider
 * 
 * @author Mike Conway - NIEHS
 *
 */
public class SpecificQueryProviderFactoryImpl implements SpecificQueryProviderFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.emc.metalnx.services.irods.utils.SpecificQueryProviderFactory#instance(
	 * org.irods.jargon.core.pub.domain.ClientHints)
	 */
	@Override
	public SpecificQueryProvider instance(final ClientHints clientHints) throws UnsupportedDataGridFeatureException {
		if (clientHints == null) {
			throw new IllegalArgumentException("null clientHints");
		}

		return instance(clientHints.whatTypeOfIcatIsIt());

	}

	@Override
	public SpecificQueryProvider instance(final IcatTypeEnum icatTypeEnum) throws UnsupportedDataGridFeatureException {
		if (icatTypeEnum == null) {
			throw new IllegalArgumentException("null icatTypeEnum");
		}

		SpecificQueryProvider provider = null;

		switch (icatTypeEnum) {
		case POSTGRES:
			provider = new PostgresSpecificQueryProviderImpl();
			break;
		case MYSQL:
			provider = new MysqlSpecificQueryProviderImpl();
			break;
		default:
			throw new UnsupportedDataGridFeatureException("unable to handle specific queries to database");
		}

		return provider;

	}

}
