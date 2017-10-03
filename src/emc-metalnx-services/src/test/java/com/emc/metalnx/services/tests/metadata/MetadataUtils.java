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

import com.emc.metalnx.core.domain.entity.DataGridMetadata;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * Utils class for Metadata tests.
 */
public class MetadataUtils {
    static void assertDataGridMetadataInPath(String path, List<DataGridMetadata> expectedMetadataList,
                                                    List<DataGridMetadata> actualMetadataList)
            throws DataGridConnectionRefusedException {

        Assert.assertEquals(expectedMetadataList.size(), actualMetadataList.size());

        for (DataGridMetadata actualMetadata: actualMetadataList) {
            assertTrue(expectedMetadataList.contains(actualMetadata));
        }
    }

    static List<DataGridMetadata> createRandomMetadata(int numberOfMetadataTags) {
        List<DataGridMetadata> metadataList = new ArrayList<>();

        for(int i = 0; i < numberOfMetadataTags; i++) {
            String attribute = "attr" + i;
            String value = "val" + i;
            String unit = "unit" + i;
            metadataList.add(new DataGridMetadata(attribute, value, unit));
        }

        return metadataList;
    }

    static List<String> createRandomMetadataAsString(int numberOfMetadataTags) {
        List<String> metadataList = new ArrayList<>();

        for(int i = 0; i < numberOfMetadataTags; i++) {
            String attribute = "attr" + i;
            String value = "val" + i;
            String unit = "unit" + i;
            metadataList.add(attribute + " " + value + " " + unit);
        }

        return metadataList;
    }
}
