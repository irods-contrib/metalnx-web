/**
 * 
 */
package com.emc.metalnx.services.irods.mail;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

/**
 * Provides a factory that can take spring proviced configuration from
 * metalnx.properties and return a configured mail service
 * 
 * @author Mike Conway - NIEHS
 *
 */
@Component
public class MailServiceFactory {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceFactory.class);

	/**
	 * {@link MailProperties} that configures the mail settings
	 */
	@Autowired
	private MailProperties mailProperties;

	public MailProperties getMailProperties() {
		return mailProperties;
	}

	public void setMailProperties(MailProperties mailProperties) {
		this.mailProperties = mailProperties;
	}

	/**
	 * Create an instance of the {@link JavaMailSender} with the given properties
	 * 
	 * @return {@link JavaMailSender}
	 */
	public JavaMailSender instance() {

		logger.info("instance()");
		if (mailProperties == null) {
			logger.error("mailProperties not configured");
			throw new IllegalStateException("mailProperties is missing");
		}

		if (!mailProperties.isEnabled()) {
			logger.error("mail services are disabled!");
			throw new IllegalStateException("mail service invoked but mail services are disabled");
		}

		logger.debug("setup of mail sender using properties:{}", mailProperties);

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		mailSender.setHost(mailProperties.getHost());
		mailSender.setPort(mailProperties.getPort());
		mailSender.setUsername(mailProperties.getUsername());
		mailSender.setPassword(mailProperties.getPassword());

		Properties javaMailProperties = new Properties();
		javaMailProperties.put("mail.smtp.starttls.enable", mailProperties.isStartTlsEnable());
		javaMailProperties.put("mail.smtp.auth", mailProperties.isSmtpAuth());
		javaMailProperties.put("mail.transport.protocol", mailProperties.getMailTransportProtocol());
		javaMailProperties.put("mail.debug", mailProperties.isDebug());

		mailSender.setJavaMailProperties(javaMailProperties);
		return mailSender;
	}

}
