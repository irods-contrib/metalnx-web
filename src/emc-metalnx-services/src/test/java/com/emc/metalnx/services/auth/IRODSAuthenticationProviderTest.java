package com.emc.metalnx.services.auth;

import java.util.Properties;

import org.irods.jargon.core.connection.AuthScheme;
import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.emc.metalnx.core.domain.dao.UserDao;

public class IRODSAuthenticationProviderTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "IRODSAuthenticationProviderTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;
	public static String rootCollPathInIrods;

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

	@After
	public void afterEach() throws Exception {
		irodsFileSystem.closeAndEatExceptions();
	}

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

	// @Test
	public void testPamUserNotIrodsUser() throws Exception {
		IRODSAccount irodsAccount = testingPropertiesHelper.buildPamIrodsAccountFromTestProperties(testingProperties);
		UserDao userDAO = Mockito.mock(UserDao.class);
		IRODSAuthenticationProvider provider = new IRODSAuthenticationProvider();
		provider.setIrodsHost(irodsAccount.getHost());
		provider.setIrodsPort(String.valueOf(irodsAccount.getPort()));
		provider.setIrodsZoneName(irodsAccount.getZone());
		provider.irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();
		provider.userDao = userDAO;
		provider.setIrodsAuthScheme(AuthScheme.PAM.toString());

		Authentication authentication = new UsernamePasswordAuthenticationToken(irodsAccount.getUserName(),
				irodsAccount.getPassword());

		provider.authenticate(authentication);

	}

}
