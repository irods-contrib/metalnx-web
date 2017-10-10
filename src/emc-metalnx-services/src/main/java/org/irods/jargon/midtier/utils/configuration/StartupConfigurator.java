/**
 * 
 */
package org.irods.jargon.midtier.utils.configuration;

import org.irods.jargon.core.connection.ClientServerNegotiationPolicy;
import org.irods.jargon.core.connection.ClientServerNegotiationPolicy.SslNegotiationPolicy;
import org.irods.jargon.core.connection.IRODSSession;
import org.irods.jargon.core.connection.SettableJargonProperties;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Mike Conway Wired-in class that takes configuration and core jargon
 *         components and injects appropriate configuration into the underlying
 *         jargon properties system
 *
 */
public class StartupConfigurator {

	private MidTierConfiguration midTierConfiguration;
	private IRODSSession irodsSession;
	private IRODSAccessObjectFactory irodsAccessObjectFactory;

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	public StartupConfigurator() {

	}

	/**
	 * @return the midTierConfiguration
	 */
	public MidTierConfiguration getMidTierConfiguration() {
		return midTierConfiguration;
	}

	/**
	 * @param midTierConfiguration
	 *            the midTierConfiguration to set
	 */
	public void setMidTierConfiguration(MidTierConfiguration midTierConfiguration) {
		this.midTierConfiguration = midTierConfiguration;
	}

	/**
	 * @return the irodsSession
	 */
	public IRODSSession getIrodsSession() {
		return irodsSession;
	}

	/**
	 * @param irodsSession
	 *            the irodsSession to set
	 */
	public void setIrodsSession(IRODSSession irodsSession) {
		this.irodsSession = irodsSession;
	}

	/**
	 * this method is wired into the spring config after the injection of the props
	 * and <code>IRODSSession</code> so that property configuration can be
	 * accomplished
	 */
	public void init() {
		log.info("init()");

		if (midTierConfiguration == null) {
			log.error("null midTierConfiguration");
			throw new IllegalStateException("null midTierConfiguration");
		}

		if (irodsSession == null) {
			log.error("null irodsSession");
			throw new IllegalStateException("null irodsSession");
		}

		log.info("configuration with:{}", midTierConfiguration);

		SettableJargonProperties props = new SettableJargonProperties(irodsSession.getJargonProperties());
		props.setComputeChecksumAfterTransfer(midTierConfiguration.isComputeChecksum());
		log.info("set checksum policy to:{}", midTierConfiguration.isComputeChecksum());

		SslNegotiationPolicy policyToSet = ClientServerNegotiationPolicy
				.findSslNegotiationPolicyFromString(midTierConfiguration.getSslNegotiationPolicy());

		log.info("policyToSet:{}", policyToSet);

		props.setNegotiationPolicy(policyToSet);
		log.info("negotiation policy set to:{}", props.getNegotiationPolicy());

		getIrodsSession().setJargonProperties(props);
		log.info("config of jargon props complete");

	}

	/**
	 * @return the irodsAccessObjectFactory
	 */
	public IRODSAccessObjectFactory getIrodsAccessObjectFactory() {
		return irodsAccessObjectFactory;
	}

	/**
	 * @param irodsAccessObjectFactory
	 *            the irodsAccessObjectFactory to set
	 */
	public void setIrodsAccessObjectFactory(IRODSAccessObjectFactory irodsAccessObjectFactory) {
		this.irodsAccessObjectFactory = irodsAccessObjectFactory;
	}

}
