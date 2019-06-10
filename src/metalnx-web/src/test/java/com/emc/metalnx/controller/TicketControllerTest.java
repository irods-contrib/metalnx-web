package com.emc.metalnx.controller;

import org.junit.Test;
import org.mockito.Mockito;

import com.emc.metalnx.core.domain.exceptions.UnsupportedDataGridFeatureException;
import com.emc.metalnx.services.configuration.GlobalConfig;
import com.emc.metalnx.services.interfaces.ConfigService;

public class TicketControllerTest {

	@Test(expected = UnsupportedDataGridFeatureException.class)
	public void testIndex() throws Exception {
		GlobalConfig globalConfig = new GlobalConfig();
		globalConfig.setTicketsEnabled(false);
		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getGlobalConfig()).thenReturn(globalConfig);
		TicketController ticketController = new TicketController();
		ticketController.setConfigService(configService);
		ticketController.index();
	}

}
