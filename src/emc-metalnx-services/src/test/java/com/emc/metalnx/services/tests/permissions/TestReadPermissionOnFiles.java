 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.permissions;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.enums.DataGridPermType;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.PermissionsService;
import com.emc.metalnx.services.interfaces.UploadService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static junit.framework.Assert.assertTrue;

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestReadPermissionOnFiles {
    private static final String BASE_FILE_NAME = "test-file-";
    private static final String RESOURCE = "demoResc";
    private static final int NUMBER_OF_FILES = 3;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private UploadService us;

    @Autowired
    private CollectionService cs;

    @Autowired
    private FileOperationService fos;

    @Autowired
    private PermissionsService permissionsService;

    private String targetPath, parentPath;

    @Before
    public void setUp() throws DataGridException {
        parentPath = String.format("/%s/home/%s", zone, username);
        targetPath = String.format("%s/test-metadata-search-%d", parentPath, System.currentTimeMillis());

        fos.deleteCollection(targetPath, true);
        cs.createCollection(new DataGridCollectionAndDataObject(targetPath, parentPath, true));

        uploadTestFiles();
    }

    @After
    public void tearDown() throws DataGridException {
        setOwnOnFiles();
        fos.deleteCollection(targetPath, true);
    }

    @Test
    public void testSetReadPermissionOnFiles() throws DataGridConnectionRefusedException {
        assertPermissionFiles(DataGridPermType.READ, false, false);
    }

    @Test
    public void testSetWritePermissionOnFiles() throws DataGridConnectionRefusedException {
        assertPermissionFiles(DataGridPermType.WRITE, false, false);
    }

    @Test
    public void testSetOwnPermissionOnFiles() throws DataGridConnectionRefusedException {
        assertPermissionFiles(DataGridPermType.OWN, false, false);
    }

    @Test
    public void testSetNonePermissionOnFiles() throws DataGridConnectionRefusedException {
        assertPermissionFiles(DataGridPermType.NONE, false, false);
    }

    private void assertPermissionFiles(DataGridPermType permission, boolean recursive, boolean inAdminMode)
            throws DataGridConnectionRefusedException {
        boolean isPermSet;

        String[] files = new String[NUMBER_OF_FILES];
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            files[i] = String.format("%s/%s", targetPath, BASE_FILE_NAME + i);
        }

        isPermSet = permissionsService.setPermissionOnPath(permission, username, recursive, inAdminMode, files);
        assertTrue(isPermSet);
    }

    private void setOwnOnFiles() throws DataGridConnectionRefusedException {
        String[] files = new String[NUMBER_OF_FILES];
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            files[i] = String.format("%s/%s", targetPath, BASE_FILE_NAME + i);
        }

        permissionsService.setPermissionOnPath(DataGridPermType.OWN, username, false, false, files);
    }

    private void uploadTestFiles() throws DataGridException {
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            String filename = BASE_FILE_NAME + i;
            MockMultipartFile file = new MockMultipartFile(filename, "Hello World".getBytes());
            us.upload(file, targetPath, false, false, "", RESOURCE, false);
        }
    }
}
