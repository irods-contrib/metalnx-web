 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.metadata;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.MetadataService;
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

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestMetadataCase {
    private static final String BASE_FILE_NAME = "test-file-";
    private static final String RESOURCE = "demoResc";
    private static final int NUMBER_OF_FILES = 3;

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${jobs.irods.username}")
    private String username;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private UploadService us;

    @Autowired
    private CollectionService cs;

    @Autowired
    private FileOperationService fos;

    private String targetPath, parentPath, attr, val, unit;

    private List<DataGridCollectionAndDataObject> objs;

    private List<DataGridMetadataSearch> search;

    @Before
    public void setUp() throws DataGridException {
        parentPath = String.format("/%s/home/%s", zone, username);
        targetPath = String.format("%s/test-metadata-search", parentPath);

        fos.deleteCollection(targetPath, true);
        cs.createCollection(new DataGridCollectionAndDataObject(targetPath, parentPath, true));

        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            String filename = BASE_FILE_NAME + i + ".txt";
            MockMultipartFile file = new MockMultipartFile(filename, "Hello World".getBytes());
            us.upload(file, targetPath, false, false, "", RESOURCE, false);

            String filepath = String.format("%s/%s", targetPath, filename);

            metadataService.addMetadataToPath(filepath, "TEST", "TEST", "TEST");
            metadataService.addMetadataToPath(filepath, "test", "test", "test");
            metadataService.addMetadataToPath(filepath, "TeSt", "tEsT", "teST");
        }

        attr = "test";
        val = "TEST";
        unit = "TEst";

        search = new ArrayList<>();
    }

    @After
    public void tearDown() throws DataGridException {
        fos.deleteCollection(targetPath, true);
    }

    @Test
    public void testCaseInsensitiveMetadataSearchEqual() throws DataGridConnectionRefusedException {
        search.add(new DataGridMetadataSearch(attr, val, unit, DataGridSearchOperatorEnum.EQUAL));
        assertMetadataSearch(3, 1);
    }

    @Test
    public void testCaseInsensitiveMetadataSearchContains() throws DataGridConnectionRefusedException {
        search.add(new DataGridMetadataSearch(attr, val, unit, DataGridSearchOperatorEnum.LIKE));
        assertMetadataSearch(3, 1);
    }

    private void assertMetadataSearch(int expectedNumOfFiles, int expectedNumOfMatchesByFile)
            throws DataGridConnectionRefusedException {
        objs = metadataService.findByMetadata(search, new DataGridPageContext(), 1, 100);

        assertTrue(objs.size() >= expectedNumOfFiles);

        for(DataGridCollectionAndDataObject obj: objs) {
            assertTrue(obj.isVisibleToCurrentUser());
            assertFalse(obj.isCollection());
            assertEquals(expectedNumOfMatchesByFile, obj.getNumberOfMatches());
        }
    }
}
