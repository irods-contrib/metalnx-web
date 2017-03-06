package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.entity.DataGridServer;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.machine.enums.ServerRequestInfoType;
import com.emc.metalnx.services.machine.util.ServerUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertEquals;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestServerUtil {

    @Test
    public void testNullJsonResponse() throws DataGridConnectionRefusedException {
        DataGridServer server = new DataGridServer();
        server.setHostname("icat.test.com");
        ServerUtil.populateDataGridServerStatus(null, server);

        assertEquals(server.getMachineStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getDataGridStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getDiskStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getMemoryStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
    }

    @Test
    public void testEmptyJsonResponse() throws DataGridConnectionRefusedException {
        DataGridServer server = new DataGridServer();
        server.setHostname("icat.test.com");
        ServerUtil.populateDataGridServerStatus("", server);

        assertEquals(server.getMachineStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getDataGridStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getDiskStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
        assertEquals(server.getMemoryStatus(), ServerRequestInfoType.WARNING_STATUS.toString());
    }
}
