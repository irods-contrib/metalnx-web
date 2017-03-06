package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.entity.DataGridMSIByServer;
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

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestMSIService {

    @InjectMocks
    private MSIService msiService;

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
        msiService = spy(MSIServiceImpl.class); // partial mocking

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
    public void testEmptyHost() throws DataGridConnectionRefusedException {
        assertNull(msiService.getMSIsInstalled(""));
    }

    @Test
    public void testNullHost() throws DataGridConnectionRefusedException {
        assertNull(msiService.getMSIsInstalled(null));
    }

    @Test
    public void testGetMSIInstalledFor420Server() throws DataGridConnectionRefusedException, DataGridRuleException {
        List<String> msis = new ArrayList<>(mlxMSIList);
        msis.addAll(irods42MSIs);
        msis.addAll(otherMSIList);

        when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msis);
        when(irodsServices.isAtLeastIrods420()).thenReturn(true);
        DataGridMSIByServer dbMSIByServer = msiService.getMSIsInstalled("server1.test.com");

        assertTrue(dbMSIByServer.isThereAnyMSI());
        assertMap(mlxMSIList, dbMSIByServer.getMetalnxMSIs());
        assertMap(irods42MSIs, dbMSIByServer.getIRODSMSIs());
        assertTrue(dbMSIByServer.getOtherMSIs().containsAll(otherMSIList));
    }

    @Test
    public void testGetMSIInstalledFor41XServer() throws DataGridConnectionRefusedException, DataGridRuleException {
        List<String> msis = new ArrayList<>(mlxMSIList);
        msis.addAll(irods41XMSIs);
        msis.addAll(otherMSIList);

        when(mockRuleService.execGetMSIsRule(anyString())).thenReturn(msis);
        when(irodsServices.isAtLeastIrods420()).thenReturn(false);
        DataGridMSIByServer dbMSIByServer = msiService.getMSIsInstalled("server1.test.com");

        assertTrue(dbMSIByServer.isThereAnyMSI());
        assertMap(mlxMSIList, dbMSIByServer.getMetalnxMSIs());
        assertMap(irods41XMSIs, dbMSIByServer.getIRODSMSIs());
        assertTrue(dbMSIByServer.getOtherMSIs().containsAll(otherMSIList));
    }

    public void assertMap(List<String> msiList, Map<String, Boolean> map) {
        for(String msi: msiList) {
            assertTrue(map.containsKey(msi));
            assertTrue(map.get(msi));
        }
    }
}