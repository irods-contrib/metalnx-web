package org.irods.metalnx.jwt;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = JwtTestConfig.class)
@WebAppConfiguration
public class JwtManagementWrapperServiceTest {

	// autowire in the beans to test from the TransferServiceApp application context
	@Autowired
	JwtManagementWrapperService jwtManagementWrapperService;

	@Test
	public void testEncodeJwt() {
		Assert.assertNotNull("no jwtManagementWrapperService", jwtManagementWrapperService);
		String jwt = jwtManagementWrapperService.encodeJwtForUser("test");
		Assert.assertNotNull("No jwt", jwt);
	}

}
