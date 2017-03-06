package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.entity.DataGridMSIByServer;
import com.emc.metalnx.core.domain.entity.DataGridMSIPkgInfo;
import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.MSIService;
import com.emc.metalnx.services.interfaces.ResourceService;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.irods.MSIServiceImpl;
import org.irods.jargon.core.exception.JargonException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestPluginService {

    @InjectMocks
    private MSIService msiService = new MSIServiceImpl();

    @Mock
    private ResourceService mockResourceService;

    @Mock
    private RuleService mockRuleService;

    @Mock
    private IRODSServices irodsServices;

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
        otherMSIList = msiUtils.getOtherMSIList();
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

        ReflectionTestUtils.setField(msiService, "msiAPIVersionSupported", msiVersion);
        ReflectionTestUtils.setField(msiService, "msiMetalnxListExpected", mlxMSIList);
        ReflectionTestUtils.setField(msiService, "irods41XMSIList", irods41XMSIs);
        ReflectionTestUtils.setField(msiService, "irods42MSIList", irods42MSIs);

        when(mockResourceService.getAllResourceServers(anyListOf(DataGridResource.class))).thenReturn(servers);
        when(mockRuleService.execGetVersionRule(anyString())).thenReturn(msiVersion);
        when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msiList);
    }

    @Test
    public void testMSIListForIRODS420() throws DataGridConnectionRefusedException, DataGridRuleException {
        when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(new ArrayList<>());
        when(irodsServices.isAtLeastIrods420()).thenReturn(true);
        DataGridMSIByServer dbMSIByServer = msiService.getMSIsInstalled("server1.test.com");
        assertFalse(dbMSIByServer.isThereAnyMSI());
    }

    @Test
    public void testMSIListForIRODS41X() throws DataGridConnectionRefusedException, DataGridRuleException {
        when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(new ArrayList<>());
        when(irodsServices.isAtLeastIrods420()).thenReturn(false);
        DataGridMSIByServer dbMSIByServer = msiService.getMSIsInstalled("server1.test.com");
        assertFalse(dbMSIByServer.isThereAnyMSI());
    }

    @Test
    public void testMSIInstalledList() throws DataGridConnectionRefusedException {
        DataGridMSIByServer dbMSIByServer = msiService.getMSIsInstalled("server1.test.com");
        Map<String, Boolean> mlxMSIsMap = dbMSIByServer.getMetalnxMSIs();
        Map<String, Boolean> iRODSMSIsMap = dbMSIByServer.getIRODSMSIs();
        List<String> otherMSIsList = dbMSIByServer.getOtherMSIs();

        for (String msi: irods41XMSIs) assertTrue(iRODSMSIsMap.containsKey(msi));
        for (String msi: mlxMSIList) assertTrue(mlxMSIsMap.containsKey(msi));
        for (String msi: otherMSIList) assertTrue(otherMSIsList.contains(msi));
    }

    @Test
    public void testNoPkgMissing() throws DataGridConnectionRefusedException, DataGridRuleException {
        DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
        assertFalse(msiPkgInfo.isThereAnyPkgMissing());
    }

    @Test
    public void testNoPkgNotSupported() throws DataGridConnectionRefusedException, DataGridRuleException {
        DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
        assertFalse(msiPkgInfo.isThereAnyPkgNotSupported());
    }

    @Test
    public void testServers() throws DataGridConnectionRefusedException, DataGridRuleException {
        DataGridMSIPkgInfo msiPkgInfo = msiService.getMSIPkgInfo();
        assertEquals(2, msiPkgInfo.getServers().size());
        for (DataGridServer server: msiPkgInfo.getServers()) assertEquals(msiVersion, server.getMSIVersion());
    }

    @Test
    public void testMSICompatibility() throws DataGridConnectionRefusedException, DataGridRuleException {
        DataGridResource resc = new DataGridResource();
        resc.setName("demoResc");
        List<DataGridResource> rescs = new ArrayList<>();
        rescs.add(resc);

        servers.get(0).setResources(rescs);

        assertTrue(msiService.isMSIAPICompatibleInResc("demoResc"));
    }

    @Test
    public void testMSICompatibilityInEmptyResc() throws DataGridConnectionRefusedException, DataGridRuleException {
        assertFalse(msiService.isMSIAPICompatibleInResc(""));
    }
}