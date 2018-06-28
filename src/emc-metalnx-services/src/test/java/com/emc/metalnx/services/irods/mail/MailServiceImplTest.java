package com.emc.metalnx.services.irods.mail;

import java.util.Properties;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.emc.metalnx.core.domain.utils.MetalnxTestUtils;
import com.emc.metalnx.core.domain.utils.TestingMetalnxPropertiesHelper;
import com.emc.metalnx.services.interfaces.mail.Mail;

public class MailServiceImplTest {

	private static Properties metalnxProperties = new Properties();
	private static TestingMetalnxPropertiesHelper metalnxPropertiesHelper = new TestingMetalnxPropertiesHelper();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingMetalnxPropertiesHelper metalnxPropertiesLoader = new TestingMetalnxPropertiesHelper();
		metalnxProperties = metalnxPropertiesLoader.getTestProperties();

	}

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

	@Test
	public void testSendEmail() throws Exception {

		if (metalnxPropertiesHelper.isMailEnabled(metalnxProperties)) {
			return;
		}

		MailProperties mailProperties = new MailProperties();
		mailProperties.setDebug(
				metalnxPropertiesHelper.getPropertyValueAsBoolean(metalnxProperties, MetalnxTestUtils.MAIL_DEBUG));
		mailProperties.setEnabled(true);
		mailProperties.setHost(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_HOST));
		mailProperties
				.setPort(metalnxPropertiesHelper.getPropertyValueAsInt(metalnxProperties, MetalnxTestUtils.MAIL_PORT));
		mailProperties
				.setMailTransportProtocol(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_TRANSPORT_PROTOCOL));
		mailProperties.setSmtpAuth(
				metalnxPropertiesHelper.getPropertyValueAsBoolean(metalnxProperties, MetalnxTestUtils.MAIL_SMTP_AUTH));
		mailProperties.setStartTlsEnable(
				metalnxPropertiesHelper.getPropertyValueAsBoolean(metalnxProperties, MetalnxTestUtils.MAIL_STARTTLS));
		mailProperties.setUsername(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_USERNAME));
		mailProperties.setPassword(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_PASSWORD));

		MailServiceFactory mailServiceFactory = new MailServiceFactory();
		mailServiceFactory.setMailProperties(mailProperties);

		MailServiceImpl mailServiceImpl = new MailServiceImpl();
		mailServiceImpl.setMailProperties(mailProperties);
		mailServiceImpl.setMailServiceFactory(mailServiceFactory);
		mailServiceImpl.init();

		Mail mail = new Mail();
		mail.setMailContent("test");
		mail.setMailFrom(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_FROM));
		mail.setMailTo(metalnxProperties.getProperty(MetalnxTestUtils.MAIL_TO));
		mail.setMailSubject("test");

		mailServiceImpl.sendEmail(mail);

		Assert.assertNull("mail sender created when disabled", mailServiceImpl.getJavaMailSender());

	}

}
