package com.emc.metalnx.services.configuration;

import org.junit.Assert;
import org.junit.Test;

public class ConfigServiceImplTest {

	@Test
	public void testGetGlobalConfig() {
		ConfigServiceImpl configService = new ConfigServiceImpl();
		configService.setTicketsEnabled(true);
		GlobalConfig config = configService.getGlobalConfig();
		Assert.assertTrue("did not set tickets enabled", config.isTicketsEnabled());

	}

}
