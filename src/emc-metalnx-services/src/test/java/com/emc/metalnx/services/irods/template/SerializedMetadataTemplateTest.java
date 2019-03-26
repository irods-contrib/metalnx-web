package com.emc.metalnx.services.irods.template;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Testing json serialization/desearialization
 * 
 * @author conwaymc
 *
 */
public class SerializedMetadataTemplateTest {

	@Test
	public void testRoundTripJson() throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		SerializedMetadataTemplate metadataTemplate = new SerializedMetadataTemplate();
		metadataTemplate.setAccessType("PUBLIC");
		metadataTemplate.setDescription("desc");
		metadataTemplate.setIdentifier("0");
		metadataTemplate.setTemplateName("boo");
		metadataTemplate.setTemplateOwner("me");
		metadataTemplate.setVersion(0);

		SerializedTemplateField field1 = new SerializedTemplateField();
		field1.setAttribute("a1");
		field1.setValue("foo");
		field1.setFieldOrder(0);

		metadataTemplate.getTemplateFields().add(field1);

		SerializedTemplateField field2 = new SerializedTemplateField();
		field1.setAttribute("a2");
		field1.setFieldOrder(1);

		metadataTemplate.getTemplateFields().add(field2);

		String json = objectMapper.writeValueAsString(metadataTemplate);

		Assert.assertNotNull("null json returned", json);

		SerializedMetadataTemplate actual = objectMapper.readValue(json, SerializedMetadataTemplate.class);
		Assert.assertNotNull("null object from input json", actual);

	}

}
