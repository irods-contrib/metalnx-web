package com.emc.metalnx.services.tests.metadata;

import com.emc.metalnx.core.domain.entity.DataGridMetadata;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import org.junit.Assert;

import java.util.List;

import static junit.framework.Assert.assertTrue;

/**
 * Utils class for Metadata tests.
 */
public class MetadataUtils {
    public static void assertDataGridMetadataInPath(String path, List<DataGridMetadata> expectedMetadataList,
                                                    List<DataGridMetadata> actualMetadataList)
            throws DataGridConnectionRefusedException {

        Assert.assertEquals(expectedMetadataList.size(), actualMetadataList.size());

        for (DataGridMetadata actualMetadata: actualMetadataList) {
            assertTrue(expectedMetadataList.contains(actualMetadata));
        }
    }
}
