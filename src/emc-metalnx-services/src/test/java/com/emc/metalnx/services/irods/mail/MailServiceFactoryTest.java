package com.emc.metalnx.services.irods.mail;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;

public class MailServiceFactoryTest {

	@Test
	public void testInstance() {

		MailProperties props = new MailProperties();
		props.setDebug(false);
		props.setDefaultEncoding("UTF-8");
		props.setEnabled(true);
		props.setHost("host");
		props.setMailTransportProtocol("smtp");
		props.setPassword("password");
		props.setPort(1247);
		props.setSmtpAuth(true);
		props.setStartTlsEnable(true);
		props.setUsername("username");

		MailServiceFactory factory = new MailServiceFactory();
		factory.setMailProperties(props);
		JavaMailSender sender = factory.instance();
		Assert.assertNotNull("null sender returned from factory", sender);

	}

}
