 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.tests.resource;

import com.emc.metalnx.core.domain.entity.DataGridResource;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.services.interfaces.ResourceService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Resource service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestResourceService {

    public static final String RODSADMIN = "rodsadmin";

    @Value("${irods.zoneName}")
    private String zone;

    @Value("${irods.host}")
    private String host;

    @Autowired
    private ResourceService resourceService;

    private String parentRescName, childRescName;

    private DataGridResource parentResc, childResc;

    @Before
    public void setUp() throws DataGridConnectionRefusedException {
        long time = System.currentTimeMillis();
        parentRescName = "testResc" + time;
        childRescName = "testRescChild" + time;
        Date date = new Date();

        parentResc = new DataGridResource();
        parentResc.setName(parentRescName);
        parentResc.setType("compound");
        parentResc.setZone(zone);
        parentResc.setCreateTime(date);
        parentResc.setModifyTime(date);
        parentResc.setFreeSpaceDate(date);
        parentResc.setPath("/var/lib/irods/iRODS/Vault2");
        parentResc.setHost(host);

        childResc = new DataGridResource();
        childResc.setName(childRescName);
        childResc.setType("unixfilesystem");
        childResc.setZone(zone);
        childResc.setCreateTime(date);
        childResc.setModifyTime(date);
        childResc.setFreeSpaceDate(date);
        childResc.setPath("/var/lib/irods/iRODS/Vault2");
        childResc.setHost(host);

        resourceService.createResource(parentResc);
    }

    @After
    public void tearDown() throws DataGridConnectionRefusedException {
        if (resourceService.find(parentRescName) != null)
            resourceService.deleteResource(parentRescName);

        if (resourceService.find(childRescName) != null)
            resourceService.deleteResource(childRescName);
    }

    @Test
    public void testDeleteResourceByName() {
        assertTrue(resourceService.deleteResource(parentRescName));
    }

    @Test
    public void testDeleteResourceWithChildren() throws DataGridConnectionRefusedException {
        resourceService.createResource(childResc);
        resourceService.addChildToResource(parentRescName, childRescName);

        assertTrue(resourceService.deleteResource(parentRescName));
        assertNotNull(resourceService.find(childRescName));
    }


}
