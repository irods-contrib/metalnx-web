 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.irodsservices;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestIRodsVersion {

    @Autowired
    private IRODSServices irodsServices;

    @Test
    public void testFindIRodsVersion() throws DataGridConnectionRefusedException {
        String version = irodsServices.findIRodsVersion();
        Assert.assertNotNull(version);
        Assert.assertFalse(version.isEmpty());
        Assert.assertTrue(version.matches("^\\d+.\\d+.\\d+$"));
    }
}
