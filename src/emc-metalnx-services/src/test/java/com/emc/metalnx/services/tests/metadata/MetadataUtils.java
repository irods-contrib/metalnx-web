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
