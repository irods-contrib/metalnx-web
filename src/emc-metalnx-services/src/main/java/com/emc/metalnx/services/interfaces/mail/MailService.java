package com.emc.metalnx.services.interfaces.mail;

import javax.mail.SendFailedException;
/**
 * Interface describing basic mail messaging service, configured through
 * {@MailProperties} in a deployment situation. This service must be enabled and
 * configured in metalnx.properties or mail messages are logged and ignored.
 * 
 * @author Mike Conway - NIEHS
 *
 */
public interface MailService {

	/**
	 * Is mail enabled in metalnx.properties?
	 * 
	 * @return {@code boolean}
	 */
	boolean isMailEnabled();

	/**
	 * Send the provided email message. Messages will be logged and ignored if mail
	 * is not enabled in the properties config.
	 * 
	 * @param {@link
	 * 			Mail} with message contents
	 */
	void sendEmail(Mail mail) throws SendFailedException;

}