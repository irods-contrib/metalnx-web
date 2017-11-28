package com.emc.metalnx.services.irods.utils;

import org.irods.jargon.core.protovalues.IcatTypeEnum;
import org.irods.jargon.core.pub.domain.ClientHints;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SpecificQueryProviderFactoryImplTest {

	@Test
	public void testPostgres() throws Exception {
		ClientHints clientHints = Mockito.mock(ClientHints.class);
		Mockito.when(clientHints.whatTypeOfIcatIsIt()).thenReturn(IcatTypeEnum.POSTGRES);
		SpecificQueryProviderFactory specificQueryProviderFactory = new SpecificQueryProviderFactoryImpl();
		SpecificQueryProvider actual = specificQueryProviderFactory.instance(clientHints);
		Assert.assertNotNull("null provider", actual);
	}

}
