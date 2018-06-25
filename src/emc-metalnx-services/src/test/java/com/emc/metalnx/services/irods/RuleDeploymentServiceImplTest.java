package com.emc.metalnx.services.irods;

import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.midtier.utils.configuration.MidTierConfiguration;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.emc.metalnx.services.interfaces.ConfigService;

public class RuleDeploymentServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "RuleDeploymentServiceImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;
	public static String rootCollPathInIrods;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		irodsFileSystem = IRODSFileSystem.instance();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@Test
	public void testCreateRuleCacheWhenNotExists() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAdminAccountFromTestProperties(testingProperties);
		MidTierConfiguration midTierConfiguration = new MidTierConfiguration();
		midTierConfiguration.setIrodsAdminAccountAuthScheme(irodsAccount.getAuthenticationScheme().toString());
		midTierConfiguration.setIrodsAdminAccountHost(irodsAccount.getHost());
		midTierConfiguration.setIrodsAdminAccountPassword(irodsAccount.getPassword());
		midTierConfiguration.setIrodsAdminAccountPort(irodsAccount.getPort());
		midTierConfiguration.setIrodsAdminAccountUser(irodsAccount.getUserName());
		midTierConfiguration.setIrodsAdminAccountZone(irodsAccount.getZone());

		IRODSServicesImpl irodsServices = new IRODSServicesImpl();
		irodsServices.setIrodsAccount(irodsAccount);
		irodsServices.midTierConfiguration = midTierConfiguration;
		irodsServices.irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		ConfigService configService = Mockito.mock(ConfigService.class);
		Mockito.when(configService.getIrodsZone()).thenReturn(irodsAccount.getZone());

		RuleDeploymentServiceImpl ruleDeploymentService = new RuleDeploymentServiceImpl();
		ruleDeploymentService.setIrodsServices(irodsServices);
		ruleDeploymentService.setConfigService(configService);

		String ruleCacheFilePath = ruleDeploymentService.getRuleCachePath();
		IRODSFile ruleCacheFile = irodsFileSystem.getIRODSFileFactory(irodsAccount)
				.instanceIRODSFile(ruleCacheFilePath);
		ruleCacheFile.deleteWithForceOption();

		Assert.assertFalse("rule cache should not exist", ruleDeploymentService.ruleCacheExists());

		ruleDeploymentService.createRuleCache();

		Assert.assertTrue("rule cache should  exist", ruleDeploymentService.ruleCacheExists());

	}

}
