package com.emc.metalnx.services.rules.tests;

import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.RuleService;
import com.emc.metalnx.services.interfaces.UploadService;
import com.emc.metalnx.services.irods.IRODSServicesImpl;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.Stream2StreamAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestRuleService {
    @Autowired private IRODSAccessObjectFactory iaof;

    @Autowired private IRODSAccount ia;

    @Autowired private RuleService rs;

    @Autowired private UploadService us;

    @Value("${irods.zoneName}")
    private String irodsZone;

    @Value("${jobs.irods.username}")
    private String username;

    private static String path;

    private IRODSServices mockedIRODSServices;

    private static final int MEGABYTE = 1024 * 1024;

    @PostConstruct
    public void init() {
        path = String.format("/%s/home/%s", irodsZone, username);

    }

    @Before
    public void setUp() throws JargonException, DataGridException, URISyntaxException, IOException {
        mockedIRODSServices = mock(IRODSServicesImpl.class);
        when(mockedIRODSServices.getRuleProcessingAO()).thenReturn(iaof.getRuleProcessingAO(ia));
        when(mockedIRODSServices.getIRODSFileFactory()).thenReturn(iaof.getIRODSFileFactory(ia));

        String filename = "test_bam_file.bam";
        IRODSFileFactory irodsFileFactory = mockedIRODSServices.getIRODSFileFactory();
        IRODSFile targetFile = irodsFileFactory.instanceIRODSFile(path, filename);
        targetFile.setResource("demoResc");

        URL url = this.getClass().getClassLoader().getResource(filename);
        File file = new File(url.toURI());
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filename);
        Stream2StreamAO stream2StreamA0 = mockedIRODSServices.getStream2StreamAO();
        stream2StreamA0.transferStreamToFileUsingIOStreams(inputStream, (File) targetFile, 0, MEGABYTE);
        inputStream.close();
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }
}
