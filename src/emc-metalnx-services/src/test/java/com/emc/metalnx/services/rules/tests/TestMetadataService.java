package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.UploadService;
import org.junit.After;
import org.junit.Assert;
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

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestMetadataService {
    private static final String BASE_FILE_NAME = "test-file-";
    private static final String RESOURCE = "demoResc";

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

    private String targetPath, parentPath;

    private List<DataGridCollectionAndDataObject> objs;

    @Before
    public void setUp() throws DataGridException {
        parentPath = String.format("/%s/home/%s", zone, username);
        targetPath = String.format("%s/test-metadata-search", parentPath);

        fos.deleteCollection(targetPath, true);
        cs.createCollection(new DataGridCollectionAndDataObject(targetPath, parentPath, true));

        MockMultipartFile file1 = new MockMultipartFile(BASE_FILE_NAME + "1.txt", "Hello World 1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile(BASE_FILE_NAME + "2.txt", "Hello World 2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile(BASE_FILE_NAME + "3.txt", "Hello World 3".getBytes());

        us.tranferFileDirectlyToJargon(file1.getName(), file1, targetPath, false, false, "", RESOURCE, false);
        us.tranferFileDirectlyToJargon(file2.getName(), file2, targetPath, false, false, "", RESOURCE, false);
        us.tranferFileDirectlyToJargon(file3.getName(), file3, targetPath, false, false, "", RESOURCE, false);

        metadataService.addMetadataToPath(targetPath, "TEST", "TEST", "TEST");
        metadataService.addMetadataToPath(targetPath, "test", "test", "test");
        metadataService.addMetadataToPath(targetPath, "TeSt", "tEsT", "teST");

        String attr = "test";
        String val = "TEST";
        String unit = "TEst";

        List<DataGridMetadataSearch> search = new ArrayList<>();
        search.add(new DataGridMetadataSearch(attr, val, unit, DataGridSearchOperatorEnum.EQUAL));

        objs = metadataService.findByMetadata(search, new DataGridPageContext(), 0, 100);
    }

    @After
    public void tearDown() throws DataGridException {
        fos.deleteCollection(targetPath, true);
    }

    @Test
    public void testCaseInsensitiveMetadataSearch() {
        Assert.assertEquals(3, objs.size());

        for(DataGridCollectionAndDataObject obj: objs) {
            Assert.assertTrue(obj.isVisibleToCurrentUser());
            Assert.assertFalse(obj.isCollection());
            Assert.assertTrue(obj.getResourceName().equalsIgnoreCase(RESOURCE));
            Assert.assertEquals(1, obj.getNumberOfMatches());
        }
    }
}
