/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.tests.msi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MSIService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestMSIService {
	@Autowired
	private MSIService msiService;

	@Mock
	private ResourceService mockResourceService;

	@Mock
	private RuleService mockRuleService;

	@Mock
	private IRODSServices irodsServices;

	@Mock
	private ConfigService mockConfigService;

	private static String msiVersion;
	private List<String> msiList, mlxMSIList, irods41XMSIs, irods42MSIs, otherMSIList;

	private List<DataGridServer> servers;

	@Before
	public void setUp() throws JargonException, DataGridException {

		// msiService = spy(MSIServiceImpl.class); // partial mocking
		msiService = Mockito.mock(MSIService.class);
		servers = new ArrayList<>();

		MSIUtils msiUtils = new MSIUtils();

		msiVersion = MSIUtils.getMsiVersion();
		msiList = msiUtils.getMsiList();
		mlxMSIList = msiUtils.getMlxMSIList();
		irods41XMSIs = msiUtils.getIrods41XMSIs();
		irods42MSIs = msiUtils.getIrods420MSIs();
		otherMSIList = msiUtils.getOtherMSIs();
		MockitoAnnotations.initMocks(this);

		DataGridServer s1 = new DataGridServer();
		s1.setHostname("server1.test.com");
		s1.setMSIVersion(msiVersion);
		s1.setIp("192.168.0.1");
		s1.setResources(new ArrayList<>());

		DataGridServer s2 = new DataGridServer();
		s2.setHostname("server2.test.com");
		s2.setMSIVersion(msiVersion);
		s2.setIp("192.168.0.2");
		s2.setResources(new ArrayList<>());

		servers.add(s1);
		servers.add(s2);

		when(mockResourceService.getAllResourceServers(anyListOf(DataGridResource.class))).thenReturn(servers);
		when(mockRuleService.execGetVersionRule(anyString())).thenReturn(msiVersion);
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msiList);

		when(mockConfigService.getMlxMSIsExpected()).thenReturn(mlxMSIList);
		when(mockConfigService.getIrods41MSIsExpected()).thenReturn(irods41XMSIs);
		when(mockConfigService.getIrods42MSIsExpected()).thenReturn(irods42MSIs);
		when(mockConfigService.getOtherMSIsExpected()).thenReturn(otherMSIList);
	}

	@Test
	public void testEmptyHost() throws DataGridException {
		assertNull(msiService.getMSIsInstalled(""));
	}

	@Test
	public void testNullHost() throws DataGridException {
		assertNull(msiService.getMSIsInstalled(null));
	}

	@Test
	public void testOtherMSIInstalledFor420() throws OperationNotSupportedByThisServerException, JargonException {
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(otherMSIList);
		when(irodsServices.isAtLeastIrods420()).thenReturn(true);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertTrue(server.isThereAnyMSI());
		assertMap(otherMSIList, server.getOtherMSIs());
	}

	@Test
	public void testOtherMSIInstalledFor41() throws OperationNotSupportedByThisServerException, JargonException {
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(otherMSIList);
		when(irodsServices.isAtLeastIrods420()).thenReturn(false);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertTrue(server.isThereAnyMSI());
		assertMap(otherMSIList, server.getOtherMSIs());
	}

	@Test
	public void testNoOtherMSIInstalledFor41() throws OperationNotSupportedByThisServerException, JargonException {
		when(irodsServices.isAtLeastIrods420()).thenReturn(false);
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(new ArrayList<>());
		when(mockConfigService.getOtherMSIsExpected()).thenReturn(new ArrayList<>());
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertFalse(server.isThereAnyMSI());
		assertTrue(server.getOtherMSIs().isEmpty());
	}

	@Test
	public void testNoOtherMSIListed() throws OperationNotSupportedByThisServerException, JargonException {
		String testMSI = "libmsitest_installed.so";

		List<String> otherMSIListWithEmptyString = new ArrayList<>();
		otherMSIListWithEmptyString.add("");
		otherMSIListWithEmptyString.add(testMSI);

		when(irodsServices.isAtLeastIrods420()).thenReturn(false);
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(otherMSIListWithEmptyString);
		when(mockConfigService.getOtherMSIsExpected()).thenReturn(otherMSIListWithEmptyString);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertTrue(server.isThereAnyMSI());
		assertFalse(server.getOtherMSIs().isEmpty());
		assertEquals(1, server.getOtherMSIs().size());
		assertTrue(server.getOtherMSIs().containsKey(testMSI));
	}

	@Test
	public void testGetMSIInstalledFor420Server() throws OperationNotSupportedByThisServerException, JargonException {
		List<String> msis = new ArrayList<>(mlxMSIList);
		msis.addAll(irods42MSIs);
		msis.addAll(otherMSIList);

		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msis);
		when(irodsServices.isAtLeastIrods420()).thenReturn(true);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertTrue(server.isThereAnyMSI());
		assertMap(mlxMSIList, server.getMetalnxMSIs());
		assertMap(irods42MSIs, server.getIRODSMSIs());
		assertMap(otherMSIList, server.getOtherMSIs());
	}

	@Test
	public void testGetMSIInstalledFor41XServer() throws OperationNotSupportedByThisServerException, JargonException {
		List<String> msis = new ArrayList<>(mlxMSIList);
		msis.addAll(irods41XMSIs);
		msis.addAll(otherMSIList);

		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msis);
		when(irodsServices.isAtLeastIrods420()).thenReturn(false);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");

		assertTrue(server.isThereAnyMSI());
		assertMap(mlxMSIList, server.getMetalnxMSIs());
		assertMap(irods41XMSIs, server.getIRODSMSIs());
		assertMap(otherMSIList, server.getOtherMSIs());
	}

	public void assertMap(List<String> msiList, Map<String, Boolean> map) {
		for (String msi : msiList) {
			assertTrue(map.containsKey(msi));
			assertTrue(map.get(msi));
		}
	}
}