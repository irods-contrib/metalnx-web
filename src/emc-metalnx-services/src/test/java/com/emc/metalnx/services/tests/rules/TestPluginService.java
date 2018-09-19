/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.tests.rules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.exception.OperationNotSupportedByThisServerException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MSIService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.irods.MSIServiceImpl;
import com.emc.metalnx.services.tests.msi.MSIUtils;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestPluginService {

	public static final String DELIMITER = ",";
	@InjectMocks
	private MSIService msiService = new MSIServiceImpl();

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

	@PostConstruct
	public void init() {
		servers = new ArrayList<>();
		MSIUtils msiUtils = new MSIUtils();

		msiVersion = MSIUtils.getMsiVersion();
		msiList = msiUtils.getMsiList();
		mlxMSIList = msiUtils.getMlxMSIList();
		irods41XMSIs = msiUtils.getIrods41XMSIs();
		irods42MSIs = msiUtils.getIrods420MSIs();
		otherMSIList = msiUtils.getOtherMSIs();
	}

	@Before
	public void setUp() throws JargonException, DataGridException {
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
		when(mockConfigService.getMsiAPIVersionSupported()).thenReturn(msiVersion);
	}

	@Test
	public void testMSIListForIRODS420() throws OperationNotSupportedByThisServerException, JargonException {
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(new ArrayList<>());
		when(irodsServices.isAtLeastIrods420()).thenReturn(true);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");
		assertFalse(server.isThereAnyMSI());
	}

	@Test
	public void testMSIListForIRODS41X() throws OperationNotSupportedByThisServerException, JargonException {
		when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(new ArrayList<>());
		when(irodsServices.isAtLeastIrods420()).thenReturn(false);
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");
		assertFalse(server.isThereAnyMSI());
	}

	@Test
	public void testMSIInstalledList() throws DataGridException {
		DataGridServer server = msiService.getMSIsInstalled("server1.test.com");
		Map<String, Boolean> mlxMSIsMap = server.getMetalnxMSIs();
		Map<String, Boolean> iRODSMSIsMap = server.getIRODSMSIs();
		Map<String, Boolean> otherMSIsList = server.getOtherMSIs();

		for (String msi : irods41XMSIs)
			assertTrue(iRODSMSIsMap.containsKey(msi));
		for (String msi : mlxMSIList)
			assertTrue(mlxMSIsMap.containsKey(msi));
		for (String msi : otherMSIList)
			assertTrue(otherMSIsList.containsKey(msi));
	}

	@Test
	public void testNoPkgMissing() throws DataGridException {
		DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
		assertFalse(msiPkgInfo.isThereAnyPkgMissing());
	}

	@Test
	public void testNoPkgNotSupported() throws DataGridException {
		DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
		assertFalse(msiPkgInfo.isThereAnyPkgNotSupported());
	}

	@Test
	public void testServers() throws DataGridException {
		DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
		assertEquals(2, msiPkgInfo.getServers().size());
		for (DataGridServer server : msiPkgInfo.getServers())
			assertEquals(msiVersion, server.getMSIVersion());
	}

	@Test
	public void testMSICompatibility() throws DataGridException {
		DataGridResource resc = new DataGridResource();
		resc.setName("demoResc");
		List<DataGridResource> rescs = new ArrayList<>();
		rescs.add(resc);

		servers.get(0).setResources(rescs);

		assertTrue(msiService.isMSIAPICompatibleInResc("demoResc"));
	}

	@Test
	public void testMSICompatibilityInEmptyResc() throws DataGridException {
		assertFalse(msiService.isMSIAPICompatibleInResc(""));
	}
}