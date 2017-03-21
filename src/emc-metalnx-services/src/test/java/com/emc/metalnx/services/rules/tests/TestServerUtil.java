/*
 * Copyright (c) 2015-2017, Dell Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
