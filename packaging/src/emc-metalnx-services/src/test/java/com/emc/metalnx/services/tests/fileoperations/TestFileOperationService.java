/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.tests.fileoperations;

import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridRuleException;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.irods.FileOperationServiceImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestFileOperationService {

    public static final String RODSADMIN = "rodsadmin";

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @InjectMocks
    private FileOperationService fileOperationService;

    @Mock
    private RuleService ruleService;

    @Mock
    private IRODSServices irodsServices;

    private DataGridUser user;

    private String path;

    @Before
    public void setUp() {
        path = String.format("/%s/home/%s", zone, username);

        user = new DataGridUser();
        user.setUsername(username);
        user.setUserType(RODSADMIN);

        fileOperationService = spy(FileOperationServiceImpl.class); // partial mocking

        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testEmptyTrashRuleException() throws DataGridConnectionRefusedException, DataGridRuleException {
        when(irodsServices.getDefaultStorageResource()).thenReturn("demoResc");
        doThrow(DataGridRuleException.class).when(ruleService).execEmptyTrashRule(anyString(), anyString(), anyBoolean());
        assertFalse(fileOperationService.emptyTrash(user, path));
    }

    @Test
    public void testEmptyTrash() throws DataGridConnectionRefusedException, DataGridRuleException {
        when(irodsServices.getDefaultStorageResource()).thenReturn("demoResc");
        doNothing().when(ruleService).execEmptyTrashRule(anyString(), anyString(), anyBoolean());
        assertTrue(fileOperationService.emptyTrash(user, path));
    }

    @Test
    public void testInvocationsOfEmptyTrashRule() throws DataGridConnectionRefusedException, DataGridRuleException {
        when(irodsServices.getDefaultStorageResource()).thenReturn("demoResc");
        doNothing().when(ruleService).execEmptyTrashRule(anyString(), anyString(), anyBoolean());
        fileOperationService.emptyTrash(user, path);
        verify(ruleService, times(1)).execEmptyTrashRule(anyString(), anyString(), anyBoolean());
    }

    @Test
    public void testNullPath() throws DataGridConnectionRefusedException {
        assertFalse(fileOperationService.emptyTrash(user, null));
    }

    @Test
    public void testEmptyPath() throws DataGridConnectionRefusedException {
        assertFalse(fileOperationService.emptyTrash(user, ""));
    }

    @Test
    public void testNullUser() throws DataGridConnectionRefusedException {
        assertFalse(fileOperationService.emptyTrash(null, path));
    }
}
