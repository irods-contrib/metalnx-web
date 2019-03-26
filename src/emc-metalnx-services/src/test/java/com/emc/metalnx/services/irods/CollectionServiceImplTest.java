package com.emc.metalnx.services.irods;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAndDataObjectListAndSearchAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.IRODSServices;

public class CollectionServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "CollectionServiceImplTest";
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

		// set up test structure

		String rootCollection = "CollectionServiceImplTestTestRoot";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		String irodsCollectionRootAbsolutePath = (MiscIRODSUtils.computeHomeDirectoryForIRODSAccount(irodsAccount) + '/'
				+ rootCollection);
		rootCollPathInIrods = irodsCollectionRootAbsolutePath;

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"textSearchQueryTest", 1, 2, 3, "textSearchService", ".txt", 4, 3, 2, 30);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.deleteWithForceOption();
		destFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

	}

	@Test
	/**
	 * Tests NIEHS bug 500 errors browsing to home or zone #2
	 * 
	 * @throws Exception
	 */
	public void testFindByNameForZoneHome() throws Exception {

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		IRODSServicesImpl irodsServices = new IRODSServicesImpl();
		irodsServices.setIrodsAccount(irodsAccount);
		irodsServices.irodsAccessObjectFactory = irodsFileSystem.getIRODSAccessObjectFactory();

		CollectionServiceImpl collectionService = new CollectionServiceImpl();
		collectionService.setIrodsServices(irodsServices);

		StringBuilder sb = new StringBuilder();
		sb.append('/');
		sb.append(irodsAccount.getZone());
		sb.append("/home");

		DataGridCollectionAndDataObject actual = collectionService.findByName(sb.toString());

		Assert.assertNotNull("no recs returned", actual);

	}

	@Test
	public void testGetSubCollectionsAndDataObjectsUnderPathThatMatchSearchTextPaginated() throws Exception {

		CollectionServiceImpl collectionService = new CollectionServiceImpl();
		IRODSServices irodsService = Mockito.mock(IRODSServices.class);
		AdminServices adminServices = Mockito.mock(AdminServices.class);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		CollectionAndDataObjectListAndSearchAO listAndSearchAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getCollectionAndDataObjectListAndSearchAO(irodsAccount);
		SpecificQueryAO specificQueryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		Mockito.when(irodsService.getEnvironmentalInfoAO()).thenReturn(environmentalInfoAO);
		Mockito.when(irodsService.getCollectionAndDataObjectListAndSearchAO()).thenReturn(listAndSearchAO);
		Mockito.when(adminServices.getSpecificQueryAO()).thenReturn(specificQueryAO);

		collectionService.setIrodsServices(irodsService);
		collectionService.setAdminServices(adminServices);
		List<DataGridCollectionAndDataObject> actual = collectionService
				.searchCollectionAndDataObjectsByName("textSearch");
		Assert.assertTrue("no recs returned", actual.size() > 1);

	}

}
