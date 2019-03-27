/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.tests.metadata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.pub.CollectionAO;
import org.irods.jargon.core.pub.DataObjectAO;
import org.irods.jargon.core.pub.DataTransferOperations;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.domain.AvuData;
import org.irods.jargon.core.pub.io.IRODSFile;
import org.irods.jargon.testutils.AssertionHelper;
import org.irods.jargon.testutils.IRODSTestSetupUtilities;
import org.irods.jargon.testutils.TestingPropertiesHelper;
import org.irods.jargon.testutils.filemanip.FileGenerator;
import org.irods.jargon.testutils.filemanip.ScratchFileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.emc.metalnx.core.domain.entity.DataGridMetadataSearch;
import com.emc.metalnx.core.domain.entity.DataGridPageContext;
import com.emc.metalnx.core.domain.entity.enums.DataGridSearchOperatorEnum;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.FileOperationService;
import com.emc.metalnx.services.interfaces.MetadataService;
import com.emc.metalnx.services.interfaces.UploadService;

/**
 * Test metadata service.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:test-services-context.xml")
@WebAppConfiguration
public class TestMetadataCase {
	private static final String BASE_FILE_NAME = "test-file-";
	private static final String RESOURCE = "demoResc";
	private static final int NUMBER_OF_FILES = 3;

	@Value("${irods.zoneName}")
	private String zone;

	@Value("${jobs.irods.username}")
	private String username;

	@Autowired
	private MetadataService metadataService;

	@Autowired
	private UploadService us;

	@Autowired
	private CollectionService cs;

	@Autowired
	private FileOperationService fos;

	private String targetPath, parentPath, attr, val, unit;

	private List<DataGridCollectionAndDataObject> objs;

	private List<DataGridMetadataSearch> search;

	private static Properties testingProperties = new Properties();
	private static TestingPropertiesHelper testingPropertiesHelper = new TestingPropertiesHelper();
	private static ScratchFileUtils scratchFileUtils = null;
	public static final String IRODS_TEST_SUBDIR_PATH = "TestMetadataCase";
	private static IRODSTestSetupUtilities irodsTestSetupUtilities = null;
	private static AssertionHelper assertionHelper = null;
	private static IRODSFileSystem irodsFileSystem;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		TestingPropertiesHelper testingPropertiesLoader = new TestingPropertiesHelper();
		testingProperties = testingPropertiesLoader.getTestProperties();
		scratchFileUtils = new ScratchFileUtils(testingProperties);
		scratchFileUtils.clearAndReinitializeScratchDirectory(IRODS_TEST_SUBDIR_PATH);
		irodsTestSetupUtilities = new IRODSTestSetupUtilities();
		irodsTestSetupUtilities.initializeIrodsScratchDirectory();
		irodsTestSetupUtilities.initializeDirectoryForTest(IRODS_TEST_SUBDIR_PATH);
		assertionHelper = new AssertionHelper();
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

	@Before
	public void setUp() throws DataGridException {
		parentPath = String.format("/%s/home/%s", zone, username);
		targetPath = String.format("%s/test-metadata-search", parentPath);

		fos.deleteCollection(targetPath, true);
		cs.createCollection(new DataGridCollectionAndDataObject(targetPath, parentPath, true));

		for (int i = 0; i < NUMBER_OF_FILES; i++) {
			String filename = BASE_FILE_NAME + i + ".txt";
			MockMultipartFile file = new MockMultipartFile(filename, "Hello World".getBytes());
			us.upload(file, targetPath, false, false, "", RESOURCE, false);

			String filepath = String.format("%s/%s", targetPath, filename);

			metadataService.addMetadataToPath(filepath, "TEST", "TEST", "TEST");
			metadataService.addMetadataToPath(filepath, "test", "test", "test");
			metadataService.addMetadataToPath(filepath, "TeSt", "tEsT", "teST");
		}

		attr = "test";
		val = "TEST";
		unit = "TEst";

		search = new ArrayList<>();
	}

	@After
	public void tearDown() throws DataGridException {
		fos.deleteCollection(targetPath, true);
	}

	@Test
	public void testCaseInsensitiveMetadataSearchEqual() throws DataGridException {
		search.add(new DataGridMetadataSearch(attr, val, unit, DataGridSearchOperatorEnum.EQUAL));
		assertMetadataSearch(3, 1);
	}

	@Test
	public void testCaseInsensitiveMetadataSearchContains() throws DataGridException {
		search.add(new DataGridMetadataSearch(attr, val, unit, DataGridSearchOperatorEnum.LIKE));
		assertMetadataSearch(3, 1);
	}

	/**
	 * For https://github.com/irods-contrib/metalnx-web/issues/98
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSearchFilesAndCollectionsBug98() throws Exception {
		String testSubdir = "testSearchFilesAndCollectionsBug98";
		String testFileName = "testSearchFilesAndCollectionsBug98.dat";
		String absPath = scratchFileUtils.createAndReturnAbsoluteScratchPath(IRODS_TEST_SUBDIR_PATH);
		String localFileName = FileGenerator.generateFileOfFixedLengthGivenName(absPath, testFileName, 10);

		IRODSAccount irodsAccount = testingPropertiesHelper.buildIRODSAccountFromTestProperties(testingProperties);

		// put scratch file into irods in the right place on the first resource

		String targetIrodsCollection = testingPropertiesHelper.buildIRODSCollectionAbsolutePathFromTestProperties(
				testingProperties, IRODS_TEST_SUBDIR_PATH + "/" + testSubdir);

		IRODSFile testDir = irodsFileSystem.getIRODSFileFactory(irodsAccount).instanceIRODSFile(targetIrodsCollection);
		testDir.mkdirs();

		String dataObjectAbsPath = targetIrodsCollection + '/' + testFileName;

		DataTransferOperations dto = irodsFileSystem.getIRODSAccessObjectFactory()
				.getDataTransferOperations(irodsAccount);
		dto.putOperation(localFileName, targetIrodsCollection, irodsAccount.getDefaultStorageResource(), null, null);

		DataObjectAO dataObjectAO = irodsFileSystem.getIRODSAccessObjectFactory().getDataObjectAO(irodsAccount);
		CollectionAO collectionAO = irodsFileSystem.getIRODSAccessObjectFactory().getCollectionAO(irodsAccount);

		// initialize the AVU data
		String expectedAttribName = "testfbmdattrib1bug98";
		String expectedAttribValue = "testfbmdvalue1bug98";
		String expectedAttribUnits = "test1fbmdunitsbug98";

		AvuData avuData = AvuData.instance(expectedAttribName, expectedAttribValue, expectedAttribUnits);

		dataObjectAO.deleteAVUMetadata(dataObjectAbsPath, avuData);
		dataObjectAO.addAVUMetadata(dataObjectAbsPath, avuData);
		collectionAO.deleteAllAVUMetadata(targetIrodsCollection);
		collectionAO.addAVUMetadata(targetIrodsCollection, avuData);

		// now do the query and expect the data object and collection

		List<DataGridMetadataSearch> testSearch = new ArrayList<>();
		testSearch.add(new DataGridMetadataSearch(expectedAttribName, expectedAttribValue, expectedAttribUnits,
				DataGridSearchOperatorEnum.EQUAL));
		objs = metadataService.findByMetadata(testSearch, new DataGridPageContext(), 0, 100);
		int ctr = 0;
		for (DataGridCollectionAndDataObject obj : objs) {
			if (!obj.getPath().contains("trash")) {
				ctr++;
			}
		}

		Assert.assertEquals(2, ctr);

	}

	private void assertMetadataSearch(int expectedNumOfFiles, int expectedNumOfMatchesByFile) throws DataGridException {
		objs = metadataService.findByMetadata(search, new DataGridPageContext(), 1, 100);

		// assertTrue(objs.size() >= expectedNumOfFiles); // FIXME: retest after
		// addressing metadata search

		for (DataGridCollectionAndDataObject obj : objs) {
			assertTrue(obj.isVisibleToCurrentUser());
			assertFalse(obj.isCollection());
			assertEquals(expectedNumOfMatchesByFile, obj.getNumberOfMatches());
		}
	}
}
