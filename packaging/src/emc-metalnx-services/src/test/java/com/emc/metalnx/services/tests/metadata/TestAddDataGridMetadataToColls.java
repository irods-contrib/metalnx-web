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

import java.util.List;

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestAddDataGridMetadataToColls {
    private static final String BASE_COLL_NAME = "test-coll-transfer-";
    private static final int NUMBER_OF_COLLS = 3;
    private static final int NUMBER_OF_METADATA_TAGS = 3;

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

        expectedMetadataList = MetadataUtils.createRandomMetadata(NUMBER_OF_METADATA_TAGS);

        for(int i = 0; i < NUMBER_OF_COLLS; i++) {
            String collPath = String.format("%s/%s", path, BASE_COLL_NAME + i);
            cs.createCollection(new DataGridCollectionAndDataObject(collPath, path, true));

            for(DataGridMetadata metadata: expectedMetadataList) {
                metadataService.addMetadataToPath(collPath, metadata);
            }
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
