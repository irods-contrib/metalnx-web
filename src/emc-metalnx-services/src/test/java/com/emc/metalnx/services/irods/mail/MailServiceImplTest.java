package com.emc.metalnx.services.irods.mail;

import org.junit.Assert;
import org.junit.Test;

public class MailServiceImplTest {

	@Test
	public void testEnabledInstance() {
		MailProperties mailProperties = new MailProperties();
		mailProperties.setDebug(false);
		mailProperties.setEnabled(true);
		mailProperties.setHost("host");
		mailProperties.setPort(22);
		mailProperties.setMailTransportProtocol("smtp");
		mailProperties.setSmtpAuth(false);
		mailProperties.setStartTlsEnable(false);
		mailProperties.setUsername("");
		mailProperties.setPassword("");

		MailServiceFactory mailServiceFactory = new MailServiceFactory();
		mailServiceFactory.setMailProperties(mailProperties);

		MailServiceImpl mailServiceImpl = new MailServiceImpl();
		mailServiceImpl.setMailProperties(mailProperties);
		mailServiceImpl.setMailServiceFactory(mailServiceFactory);
		mailServiceImpl.init();
		Assert.assertNotNull("no mail sender created", mailServiceImpl.getJavaMailSender());

	}

	@Test
	public void testNotEnabledInstance() {
		MailProperties mailProperties = new MailProperties();
		mailProperties.setDebug(false);
		mailProperties.setEnabled(false);
		mailProperties.setHost("host");
		mailProperties.setPort(22);
		mailProperties.setMailTransportProtocol("smtp");
		mailProperties.setSmtpAuth(false);
		mailProperties.setStartTlsEnable(false);
		mailProperties.setUsername("");
		mailProperties.setPassword("");

		MailServiceFactory mailServiceFactory = new MailServiceFactory();
		mailServiceFactory.setMailProperties(mailProperties);

		MailServiceImpl mailServiceImpl = new MailServiceImpl();
		mailServiceImpl.setMailProperties(mailProperties);
		mailServiceImpl.setMailServiceFactory(mailServiceFactory);
		mailServiceImpl.init();
		Assert.assertNull("mail sender created when disabled", mailServiceImpl.getJavaMailSender());

	}

}
