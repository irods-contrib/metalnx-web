package com.emc.metalnx.services.irods;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.emc.metalnx.core.domain.dao.TemplateDao;
import com.emc.metalnx.core.domain.dao.TemplateFieldDao;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.services.irods.template.SerializedMetadataTemplate;
import com.emc.metalnx.services.irods.template.SerializedTemplateField;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TemplateServiceImplTest {

	@Test
	public void testExportMetadataTemplate() throws Exception {
		DataGridTemplate dataGridTemplate = new DataGridTemplate();
		dataGridTemplate.setAccessType("PUBLIC");
		dataGridTemplate.setCreateTs(new Date());
		dataGridTemplate.setDescription("boo");
		dataGridTemplate.setId(1L);
		dataGridTemplate.setOwner("joe");
		dataGridTemplate.setTemplateName("mytemplate");
		dataGridTemplate.setVersion(1);

		DataGridTemplateField field = new DataGridTemplateField();
		field.setAttribute("attr");
		field.setId(1);
		field.setOrder(0);
		field.setTemplate(dataGridTemplate);
		field.setValue("val");
		dataGridTemplate.getFields().add(field);
		TemplateDao templateDao = Mockito.mock(TemplateDao.class);
		Mockito.when(templateDao.findById(1)).thenReturn(dataGridTemplate);

		TemplateFieldDao templateFieldDao = Mockito.mock(TemplateFieldDao.class);

		TemplateServiceImpl impl = new TemplateServiceImpl();
		impl.setTemplateDao(templateDao);
		impl.setTemplateFieldDao(templateFieldDao);

		String json = impl.exportMetadataTemplateAsJsonString(1L);
		Assert.assertNotNull("no json returned", json);

	}

	@Test
	public void testImportMetadataTemplate() throws Exception {
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
		InputStream jsonStream = new ByteArrayInputStream(json.getBytes());

		TemplateDao templateDao = Mockito.mock(TemplateDao.class);
		Mockito.when(templateDao.save(Mockito.any(DataGridTemplate.class))).thenReturn(1L);

		TemplateFieldDao templateFieldDao = Mockito.mock(TemplateFieldDao.class);
		Mockito.when(templateFieldDao.save(Mockito.any(DataGridTemplateField.class))).thenReturn(1L);

		TemplateServiceImpl impl = new TemplateServiceImpl();
		impl.setTemplateDao(templateDao);
		impl.setTemplateFieldDao(templateFieldDao);
		String testPrefix = "pre";
		String testSuffix = "post";
		String owner = "me";

		impl.importMetadataTemplate(jsonStream, owner, testPrefix, testSuffix);
		// no error means success

	}

}
