package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.irods.IRODSServicesImpl;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.annotation.PostConstruct;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestRuleService {
    @Autowired
    private IRODSAccessObjectFactory iaof;

    @Autowired
    private IRODSAccount ia;

    @Autowired
    private RuleService rs;

    @Value("${irods.zoneName}")
    private String irodsZone;

    @Value("${jobs.irods.username}")
    private String username;

    private static String path;

    private IRODSServices mockedIRODSServices;

    @PostConstruct
    public void init() {
        path = String.format("/%s/home/%s", irodsZone, username);
    }

    @Before
    public void setUp() throws JargonException, DataGridException {
        mockedIRODSServices = mock(IRODSServicesImpl.class);
        when(mockedIRODSServices.getRuleProcessingAO()).thenReturn(iaof.getRuleProcessingAO(ia));
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }
}
