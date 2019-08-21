package org.irods.metalnx.pluggablesearch;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
import org.irods.jargon.extensions.searchplugin.model.SearchAttributes;
import org.irods.metalnx.jwt.JwtManagementWrapperService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.emc.metalnx.services.interfaces.ConfigService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = PluggableSearchTestConfig.class)
@WebAppConfiguration
public class PluggableSearchWrapperServiceTest {

	@Autowired
	ConfigService configService;

	@Autowired
	JwtManagementWrapperService jwtManagementWrapperService;

	@Autowired
	PluggableSearchWrapperService pluggableSearchWrapperService;

	@Test
	public void testEncodeAJwt() {
		SearchIndexInventory inventory = pluggableSearchWrapperService.getSearchIndexInventory();
		Assert.assertNotNull("no inventory found", inventory);
	}

	@Test
	public void testListSchemaAttributes() throws Exception {
		String testEndpoint = pluggableSearchWrapperService.getSearchPluginDiscoveryService()
				.getSearchPluginRegistrationConfig().getEndpointRegistryList().get(0);
		String testSchema = pluggableSearchWrapperService.getSearchIndexInventory().getIndexInventoryEntries()
				.get(testEndpoint).getIndexInformation().getIndexes().get(0).getId();
		SearchAttributes attribs = pluggableSearchWrapperService.listAttributes(testEndpoint, testSchema);
		Assert.assertNotNull("attribs null", attribs);
	}

}
