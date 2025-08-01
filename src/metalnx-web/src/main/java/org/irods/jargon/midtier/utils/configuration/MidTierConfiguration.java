/**
 * 
 */
package org.irods.jargon.midtier.utils.configuration;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;

/**
 * Pojo containing configuration information
 * 
 * @author Mike Conway - NIEHS
 * 
 */
public class MidTierConfiguration {

	/**
	 * requests, if true, that a checksum be computed on upload
	 */
	private boolean computeChecksum = false;

	/**
	 * Default storage resource
	 */
	private String defaultStorageResource = "";

	/**
	 * Utilize the read ahead and write behind streams in jargon to optimize
	 * transfers
	 */
	private boolean utilizePackingStreams = true;

	/**
	 * AuthScheme to use for accounts, based on {@link AuthScheme}
	 */
	private String authType = AuthScheme.STANDARD.toString();

	/**
	 * sets ssl negotiation policy in jargon
	 */
	private String sslNegotiationPolicy = ClientServerNegotiationPolicy.SslNegotiationPolicy.CS_NEG_DONT_CARE
			.toString();

	/**
	 * @return the defaultStorageResource
	 */
	public String getDefaultStorageResource() {
		return defaultStorageResource;
	}

	/**
	 * @param defaultStorageResource
	 *            the defaultStorageResource to set
	 */
	public void setDefaultStorageResource(final String defaultStorageResource) {
		this.defaultStorageResource = defaultStorageResource;
	}

	/**
	 * @return the authType
	 */
	public String getAuthType() {
		return authType;
	}

	/**
	 * @param authType
	 *            the authType to set
	 */
	public void setAuthType(String authType) {
		this.authType = authType;
	}

	/**
	 * @return the utilizePackingStreams
	 */
	public boolean isUtilizePackingStreams() {
		return utilizePackingStreams;
	}

	/**
	 * @param utilizePackingStreams
	 *            the utilizePackingStreams to set
	 */
	public void setUtilizePackingStreams(boolean utilizePackingStreams) {
		this.utilizePackingStreams = utilizePackingStreams;
	}

	/**
	 * @return the sslNegotiationPolicy
	 */
	public String getSslNegotiationPolicy() {
		return sslNegotiationPolicy;
	}

	/**
	 * @param sslNegotiationPolicy
	 *            the sslNegotiationPolicy to set
	 */
	public void setSslNegotiationPolicy(String sslNegotiationPolicy) {
		this.sslNegotiationPolicy = sslNegotiationPolicy;
	}

	/**
	 * @return the computeChecksum
	 */
	public boolean isComputeChecksum() {
		return computeChecksum;
	}

	/**
	 * @param computeChecksum
	 *            the computeChecksum to set
	 */
	public void setComputeChecksum(boolean computeChecksum) {
		this.computeChecksum = computeChecksum;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MidTierConfiguration [");
		builder.append("computeChecksum=").append(computeChecksum).append(", ");
		if (defaultStorageResource != null) {
			builder.append("defaultStorageResource=").append(defaultStorageResource).append(", ");
		}
		builder.append("utilizePackingStreams=").append(utilizePackingStreams).append(", ");
		if (authType != null) {
			builder.append("authType=").append(authType).append(", ");
		}
		if (sslNegotiationPolicy != null) {
			builder.append("sslNegotiationPolicy=").append(sslNegotiationPolicy);
		}
		builder.append("]");
		return builder.toString();
	}
}
