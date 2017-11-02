package com.emc.metalnx.services.irods;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.EnvironmentalInfoAO;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.SpecificQueryAO;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.core.pub.io.IRODSFileFactory;
import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.irods.jargon.core.utils.MiscIRODSUtils;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.services.interfaces.AdminServices;
import com.emc.metalnx.services.interfaces.IRODSServices;

import junit.framework.Assert;

public class SpecQueryServiceImplTest {

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "SpecQueryServiceImplTest";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static IRODSFileSystem irodsFileSystem;
	public static String rootCollPathInIrods;

	public static final String COLL_AVU_ATTR1 = "specQueryColl1";
	public static final String COLL_AVU_VAL1 = "specQueryCollVal1";

	public static final String COLL_AVU_ATTR2 = "specQueryColl2";
	public static final String COLL_AVU_VAL2 = "specQueryCollVal2";

	public static final String DATA_AVU_ATTR1 = "specQueryData1";
	public static final String DATA_AVU_VAL1 = "specQueryDataVal1";

	public static final String DATA_AVU_ATTR2 = "specQueryData2";
	public static final String DATA_AVU_VAL2 = "specQueryDataVal2";

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

		String rootCollection = "SpecQueryServiceImplTestRoot";
		String localCollectionAbsolutePath = scratchFileUtils
				.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH + '/' + rootCollection);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);
		String irodsCollectionRootAbsolutePath = (MiscIRODSUtils.computeHomeDirectoryForIRODSAccount(irodsAccount) + '/'
				+ rootCollection);
		rootCollPathInIrods = irodsCollectionRootAbsolutePath;

		FileGenerator.generateManyFilesAndCollectionsInParentCollectionByAbsolutePath(localCollectionAbsolutePath,
				"specQueryTset", 1, 2, 3, "testFile", ".txt", 4, 3, 2, 30000);

		IRODSFileFactory irodsFileFactory = irodsFileSystem.getIRODSFileFactory(irodsAccount);
		IRODSFile destFile = irodsFileFactory.instanceIRODSFile(irodsCollectionRootAbsolutePath);
		destFile.deleteWithForceOption();
		destFile.mkdirs();
		DataTransferOperations dataTransferOperationsAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);
		File localFile = new File(localCollectionAbsolutePath);

		dataTransferOperationsAO.putOperation(localFile, destFile, null, null);

		// Decorate with various AVUs to use in tests

		decorateChildren(destFile, collectionAO, dataObjectAO);
	}

	public static void decorateChildren(final IRODSFile file, final CollectionAO collectionAO,
			final DataObjectAO dataObjectAO) throws Exception {
		IRODSFile childIrods;
		int i = 0;
		AvuData dataToAdd;
		for (File child : file.listFiles()) {
			childIrods = (IRODSFile) child;
			if (childIrods.isDirectory()) {
				if (i % 2 == 0) {
					dataToAdd = AvuData.instance(COLL_AVU_ATTR1, COLL_AVU_VAL1, "");
				} else {
					dataToAdd = AvuData.instance(COLL_AVU_ATTR2, COLL_AVU_VAL2, "");
				}
				collectionAO.addAVUMetadata(childIrods.getAbsolutePath(), dataToAdd);
				decorateChildren(childIrods, collectionAO, dataObjectAO);
			} else {
				if (i % 2 == 0) {
					dataToAdd = AvuData.instance(DATA_AVU_ATTR1, DATA_AVU_VAL1, "");
				} else {
					dataToAdd = AvuData.instance(DATA_AVU_ATTR2, DATA_AVU_VAL2, "");
				}
				dataObjectAO.addAVUMetadata(childIrods.getAbsolutePath(), dataToAdd);
			}
		}
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
	public void testCountCollectionsMatchingMetadata() throws Exception {

		SpecQueryServiceImpl specQueryService = new SpecQueryServiceImpl();
		IRODSServices irodsService = Mockito.mock(IRODSServices.class);
		AdminServices adminServices = Mockito.mock(AdminServices.class);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		SpecificQueryAO specificQueryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		Mockito.when(irodsService.getEnvironmentalInfoAO()).thenReturn(environmentalInfoAO);
		Mockito.when(adminServices.getSpecificQueryAO()).thenReturn(specificQueryAO);

		specQueryService.setIrodsServices(irodsService);
		specQueryService.setAdminServices(adminServices);
		List<DataGridMetadataSearch> metadataSearch = new ArrayList<DataGridMetadataSearch>();
		DataGridMetadataSearch search = new DataGridMetadataSearch(COLL_AVU_ATTR1, COLL_AVU_VAL1, "",
				DataGridSearchOperatorEnum.EQUAL);
		metadataSearch.add(search);
		int count = specQueryService.countCollectionsMatchingMetadata(metadataSearch, irodsAccount.getZone());
		Assert.assertTrue("no recs returned", count > 1);

	}

	@Test
	public void testCountDataObjectsMatchingMetadata() throws Exception {
		SpecQueryServiceImpl specQueryService = new SpecQueryServiceImpl();
		IRODSServices irodsService = Mockito.mock(IRODSServices.class);
		AdminServices adminServices = Mockito.mock(AdminServices.class);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		SpecificQueryAO specificQueryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		Mockito.when(irodsService.getEnvironmentalInfoAO()).thenReturn(environmentalInfoAO);
		Mockito.when(adminServices.getSpecificQueryAO()).thenReturn(specificQueryAO);

		specQueryService.setIrodsServices(irodsService);
		specQueryService.setAdminServices(adminServices);
		List<DataGridMetadataSearch> metadataSearch = new ArrayList<DataGridMetadataSearch>();
		DataGridMetadataSearch search = new DataGridMetadataSearch(DATA_AVU_ATTR1, DATA_AVU_VAL1, "",
				DataGridSearchOperatorEnum.EQUAL);
		metadataSearch.add(search);
		int count = specQueryService.countDataObjectsMatchingMetadata(metadataSearch, irodsAccount.getZone());
		Assert.assertTrue("no recs returned", count > 1);
	}

	@Test
	public void testSearchByMetadataDataObjects() throws Exception {
		SpecQueryServiceImpl specQueryService = new SpecQueryServiceImpl();
		IRODSServices irodsService = Mockito.mock(IRODSServices.class);
		AdminServices adminServices = Mockito.mock(AdminServices.class);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		SpecificQueryAO specificQueryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		Mockito.when(irodsService.getEnvironmentalInfoAO()).thenReturn(environmentalInfoAO);
		Mockito.when(adminServices.getSpecificQueryAO()).thenReturn(specificQueryAO);

		specQueryService.setIrodsServices(irodsService);
		specQueryService.setAdminServices(adminServices);
		List<DataGridMetadataSearch> metadataSearch = new ArrayList<DataGridMetadataSearch>();
		DataGridMetadataSearch search = new DataGridMetadataSearch(DATA_AVU_ATTR1, DATA_AVU_VAL1, "",
				DataGridSearchOperatorEnum.EQUAL);
		metadataSearch.add(search);
		SpecificQueryResultSet result = specQueryService.searchByMetadata(metadataSearch, irodsAccount.getZone(), false,
				null, 0, 0);
		Assert.assertFalse("no result", result.getResults().isEmpty());
	}

	@Test
	public void testSearchByMetadataCollections() throws Exception {
		SpecQueryServiceImpl specQueryService = new SpecQueryServiceImpl();
		IRODSServices irodsService = Mockito.mock(IRODSServices.class);
		AdminServices adminServices = Mockito.mock(AdminServices.class);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		EnvironmentalInfoAO environmentalInfoAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getEnvironmentalInfoAO(irodsAccount);
		SpecificQueryAO specificQueryAO = irodsFileSystem.getIRODSAccessObjectFactory()
				.getSpecificQueryAO(irodsAccount);

		Mockito.when(irodsService.getEnvironmentalInfoAO()).thenReturn(environmentalInfoAO);
		Mockito.when(adminServices.getSpecificQueryAO()).thenReturn(specificQueryAO);

		specQueryService.setIrodsServices(irodsService);
		specQueryService.setAdminServices(adminServices);
		List<DataGridMetadataSearch> metadataSearch = new ArrayList<DataGridMetadataSearch>();
		DataGridMetadataSearch search = new DataGridMetadataSearch(COLL_AVU_ATTR1, COLL_AVU_VAL1, "",
				DataGridSearchOperatorEnum.EQUAL);
		metadataSearch.add(search);
		SpecificQueryResultSet result = specQueryService.searchByMetadata(metadataSearch, irodsAccount.getZone(), true,
				null, 0, 0);
		Assert.assertFalse("no result", result.getResults().isEmpty());
	}

	@Test
	public void testSearchByFileProperties() {
		fail("Not yet implemented");
	}

	@Test
	public void testCountCollectionsMatchingFileProperties() {
		fail("Not yet implemented");
	}

	@Test
	public void testCountDataObjectsMatchingFileProperties() {
		fail("Not yet implemented");
	}

}
