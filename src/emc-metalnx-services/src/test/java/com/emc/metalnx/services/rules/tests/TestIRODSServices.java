package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test iRODS services.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestIRODSServices {

    @Autowired
    private IRODSServices irodsServices;

    @Test
    public void testIRODSVersion() throws DataGridConnectionRefusedException {
        assertTrue(irodsServices.isAtLeastIrods420() || irodsServices.isAtLeastIrods418());
    }

    @Test
    public void testEnvironmentalInfo() throws DataGridConnectionRefusedException {
        assertNotNull(irodsServices.getEnvironmentalInfoAO());
    }
}
