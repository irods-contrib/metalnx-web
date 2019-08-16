package org.irods.metalnx.pluggablesearch;

import org.irods.jargon.extensions.searchplugin.SearchIndexInventory;
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

}
