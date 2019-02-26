/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.tests.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;

import java.util.List;

import javax.annotation.PostConstruct;

import org.irods.jargon.core.exception.JargonException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.util.ReflectionTestUtils;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.configuration.AuthTypeMapping;
import com.emc.metalnx.services.configuration.ConfigServiceImpl;
import com.emc.metalnx.services.interfaces.ConfigService;
import com.emc.metalnx.services.tests.msi.MSIUtils;

/**
 * Test for Rule Service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestConfigService {

	public static final String DELIMITER = ",";
	@InjectMocks
	private ConfigService configService;

	private static String msiVersion;
	private List<String> msiList, mlxMSIList, irods41XMSIs, irods42MSIs, otherMSIList;

	@PostConstruct
	public void init() {
		configService = spy(ConfigServiceImpl.class); // partial mocking

		MSIUtils msiUtils = new MSIUtils();

		msiVersion = MSIUtils.getMsiVersion();
		msiList = msiUtils.getMsiList();
		mlxMSIList = msiUtils.getMlxMSIList();
		irods41XMSIs = msiUtils.getIrods41XMSIs();
		irods42MSIs = msiUtils.getIrods420MSIs();
		otherMSIList = msiUtils.getOtherMSIs();
	}

	@Before
	public void setUp() throws JargonException, DataGridException {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testMissingIrods42MSIsProperty() {
		ReflectionTestUtils.setField(configService, "irods42MSIsExpected", null);
		assertTrue(configService.getIrods42MSIsExpected().isEmpty());
	}

	@Test
	public void testIrods42MSIsProperty() throws DataGridConnectionRefusedException {
		ReflectionTestUtils.setField(configService, "irods42MSIsExpected", String.join(DELIMITER, irods42MSIs));
		List<String> actualList = configService.getIrods42MSIsExpected();
		assertTrue(actualList.equals(irods42MSIs));
	}

	@Test
	public void testMissingIrods41MSIsProperty() {
		ReflectionTestUtils.setField(configService, "irods41MSIsExpected", null);
		assertTrue(configService.getIrods41MSIsExpected().isEmpty());
	}

	@Test
	public void testIrods41MSIsProperty() throws DataGridConnectionRefusedException {
		ReflectionTestUtils.setField(configService, "irods41MSIsExpected", String.join(DELIMITER, irods41XMSIs));
		List<String> actualList = configService.getIrods41MSIsExpected();
		assertTrue(actualList.equals(irods41XMSIs));
	}

	@Test
	public void testMissingMetalnxMSIsProperty() {
		ReflectionTestUtils.setField(configService, "mlxMSIsExpected", null);
		assertTrue(configService.getMlxMSIsExpected().isEmpty());
	}

	@Test
	public void testMetalnxMSIsProperty() throws DataGridConnectionRefusedException {
		ReflectionTestUtils.setField(configService, "mlxMSIsExpected", String.join(DELIMITER, mlxMSIList));
		List<String> actualList = configService.getMlxMSIsExpected();
		assertTrue(actualList.equals(mlxMSIList));
	}

	@Test
	public void testMissingOtherMSIProperty() {
		ReflectionTestUtils.setField(configService, "otherMSIsExpected", null);
		assertTrue(configService.getOtherMSIsExpected().isEmpty());
	}

	@Test
	public void testOtherMSIProperty() throws DataGridConnectionRefusedException {
		ReflectionTestUtils.setField(configService, "otherMSIsExpected", "msi1.so,msi2.so,msi3.so");
		List<String> msis = configService.getOtherMSIsExpected();
		assertTrue(msis.contains("msi1.so"));
		assertTrue(msis.contains("msi2.so"));
		assertTrue(msis.contains("msi3.so"));
	}

	@Test
	public void testMissingMSIAPIVersionProperty() {
		ReflectionTestUtils.setField(configService, "msiAPIVersionSupported", null);
		assertTrue(configService.getMsiAPIVersionSupported().isEmpty());
	}

	@Test
	public void testMSIAPIVersionProperty() throws DataGridConnectionRefusedException {
		ReflectionTestUtils.setField(configService, "msiAPIVersionSupported", msiVersion);
		assertEquals(msiVersion, configService.getMsiAPIVersionSupported());
	}

	@Test
	public void testListAuthSchemesNoProp() throws Exception {
		ConfigService configService = new ConfigServiceImpl();
		List<AuthTypeMapping> actual = configService.listAuthTypeMappings();
		Assert.assertNotNull("no authtype mappings", actual);
		Assert.assertFalse("empty mappings when going with irods default", actual.isEmpty());
	}

	@Test
	public void testListAuthSchemesViaPropertyMapping() throws Exception {
		ConfigServiceImpl configService = new ConfigServiceImpl();
		configService.setAuthtypeMappings("foo:bar|zip:zap");
		List<AuthTypeMapping> actual = configService.listAuthTypeMappings();
		Assert.assertNotNull("no authtype mappings", actual);
		Assert.assertFalse("empty mappings when going with properties default", actual.isEmpty());
		Assert.assertTrue("should be 2 mappings", actual.size() == 2);
		AuthTypeMapping expected1 = new AuthTypeMapping("foo", "bar");
		Assert.assertEquals("irods auth type mismatch", expected1.getIrodsAuthType(), actual.get(0).getIrodsAuthType());
		Assert.assertEquals("user auth type mismatch", expected1.getUserAuthType(), actual.get(0).getUserAuthType());

	}

}