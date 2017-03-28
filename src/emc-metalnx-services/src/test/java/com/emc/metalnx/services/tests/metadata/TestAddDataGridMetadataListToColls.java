package com.emc.metalnx.services.tests.metadata;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadata;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestAddDataGridMetadataListToColls {
    private static final String BASE_COLL_NAME = "test-coll-transfer-";
    private static final int NUMBER_OF_COLLS = 3;

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

    private String parentPath, path;

    private List<DataGridMetadata> expectedMetadataList;

    @Before
    public void setUp() throws DataGridException {
        parentPath = String.format("/%s/home/%s", zone, username);
        path = String.format("%s/test-metadata-transfer", parentPath);

        fos.deleteCollection(path, true);
        cs.createCollection(new DataGridCollectionAndDataObject(path, parentPath, true));

        expectedMetadataList = new ArrayList<>();
        expectedMetadataList.add(new DataGridMetadata("attr1",  "val1",  "unit1"));
        expectedMetadataList.add(new DataGridMetadata("attr2",  "val2",  "unit2"));
        expectedMetadataList.add(new DataGridMetadata("attr3",  "val3",  "unit3"));

        for(int i = 0; i < NUMBER_OF_COLLS; i++) {
            String collname = BASE_COLL_NAME + i;
            String collPath = String.format("%s/%s", path, collname);
            cs.createCollection(new DataGridCollectionAndDataObject(collPath, path, true));

            metadataService.addMetadataToPath(collPath, expectedMetadataList);
        }
    }

    @After
    public void tearDown() throws DataGridException {
        fos.deleteCollection(path, true);
    }

    @Test
    public void testAddMetadataToColls() throws DataGridConnectionRefusedException {
        for (int i = 0; i < NUMBER_OF_COLLS; i++) {
            String collPath = String.format("%s/%s", path, BASE_COLL_NAME + i);
            List<DataGridMetadata> actualMetadataList = metadataService.findMetadataValuesByPath(collPath);
            MetadataUtils.assertDataGridMetadataInPath(collPath, expectedMetadataList, actualMetadataList);
        }
    }
}
