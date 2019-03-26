/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.irods;

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.irods.jargon.core.exception.DataNotFoundException;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.TemplateDao;
import com.emc.metalnx.core.domain.dao.TemplateFieldDao;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.interfaces.TemplateService;
import com.emc.metalnx.services.irods.template.SerializedMetadataTemplate;
import com.emc.metalnx.services.irods.template.SerializedTemplateField;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

	@Autowired
	private TemplateDao templateDao;

	@Autowired
	private TemplateFieldDao templateFieldDao;

	private ObjectMapper objectMapper = new ObjectMapper();
	org.joda.time.format.DateTimeFormatter dateParser = ISODateTimeFormat.dateTimeNoMillis();

	private DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone
																			// offset

	private static final Logger logger = LoggerFactory.getLogger(TemplateServiceImpl.class);

	@Override
	public boolean modifyTemplate(DataGridTemplate template) {
		if (template == null) {
			return false;
		}

		templateDao.merge(template);

		return true;
	}

	@Override
	public DataGridTemplate findById(long id) {
		return templateDao.findById(id);
	}

	@Override
	public DataGridTemplate findByName(String templateName) {
		return templateDao.findByName(templateName);
	}

	@Override
	public long createTemplate(DataGridTemplate dataGridTemplate) {
		Date today = new Date();

		dataGridTemplate.setVersion(1);
		dataGridTemplate.setCreateTs(today);
		dataGridTemplate.setModifyTs(today);

		long id = templateDao.save(dataGridTemplate);

		return id;
	}

	@Override
	public List<DataGridTemplate> findAll() {
		List<DataGridTemplate> dataGridTemplates = templateDao.findAll(DataGridTemplate.class);

		Collections.sort(dataGridTemplates);

		return dataGridTemplates;
	}

	@Override
	public boolean deleteTemplate(long id) {
		/*
		 * we need to remove all template fields existing in a template before removing
		 * the Template itself
		 */
		List<DataGridTemplateField> templateFields = this.listTemplateFields(id);
		for (DataGridTemplateField templateField : templateFields) {
			templateFieldDao.delete(templateField);
		}

		return templateDao.deleteById(id);
	}

	@Override
	public List<DataGridTemplate> findByQueryString(String queryString) {
		List<DataGridTemplate> templates = templateDao.findByQueryString(queryString);
		Collections.sort(templates);
		return templates;
	}

	@Override
	public List<DataGridTemplateField> listTemplateFields(String template) {
		List<DataGridTemplateField> templateFields = templateDao.listTemplateFields(template);
		Collections.sort(templateFields);
		return templateFields;
	}

	@Override
	public List<DataGridTemplateField> listTemplateFields(Long id) {
		List<DataGridTemplateField> templateFields = templateDao.listTemplateFields(id);
		Collections.sort(templateFields);
		return templateFields;
	}

	@Override
	public List<DataGridTemplate> listPublicTemplates() {
		return templateDao.listPublicTemplates();
	}

	@Override
	public List<DataGridTemplate> listPrivateTemplatesByUser(String user) {
		return templateDao.listPrivateTemplatesByUser(user);
	}

	@Override
	public boolean importMetadataTemplate(InputStream inStream, String owner, String prefix, String suffix)
			throws MetadataTemplateException, DataGridException {
		logger.info("importMetadataTemplate()");

		if (inStream == null) {
			throw new IllegalArgumentException("null inStream");
		}

		if (owner == null || owner.isEmpty()) {
			throw new IllegalArgumentException("null owner");
		}

		if (prefix == null) {
			prefix = "";
		}

		if (suffix == null) {
			suffix = "";
		}

		SerializedMetadataTemplate template;
		try {
			template = objectMapper.readValue(inStream, SerializedMetadataTemplate.class);
		} catch (IOException e) {
			logger.error("error parsing metadata template", e);
			throw new MetadataTemplateException("error parsing template from JSON", e);
		}

		String newTemplateName = String.format("%s%s%s", prefix, template.getTemplateName(), suffix);
		logger.info("newTemplateName:{}", newTemplateName);

		DataGridTemplate dataGridTemplate = new DataGridTemplate();
		dataGridTemplate.setAccessType(template.getAccessType());

		/*
		 * if dates are provided, use those, otherwise init with current timestamp.
		 * Dates are in ISO data format
		 */

		if (template.getIso8601CreateDate() != null && !template.getIso8601CreateDate().isEmpty()) {
			dataGridTemplate.setCreateTs(dateParser.parseDateTime(template.getIso8601CreateDate()).toDate());
		} else {
			dataGridTemplate.setCreateTs(new Date());
		}

		if (template.getIso8601ModifyDate() != null && !template.getIso8601ModifyDate().isEmpty()) {
			dataGridTemplate.setModifyTs(dateParser.parseDateTime(template.getIso8601ModifyDate()).toDate());
		}

		dataGridTemplate.setDescription(template.getDescription());
		dataGridTemplate.setOwner(owner);
		dataGridTemplate.setTemplateName(newTemplateName);
		dataGridTemplate.setVersion(template.getVersion());
		long templateId = this.createTemplate(dataGridTemplate);
		dataGridTemplate.setId(templateId);
		for (SerializedTemplateField field : template.getTemplateFields()) {
			DataGridTemplateField templateField = new DataGridTemplateField();
			templateField.setTemplate(dataGridTemplate);
			templateField.setAttribute(field.getAttribute());
			templateField.setOrder(field.getFieldOrder());
			templateField.setTemplate(dataGridTemplate);
			templateField.setUnit(field.getUnit());
			templateField.setValue(field.getValue());
			templateFieldDao.save(templateField);
		}

		return true;
	}

	@Override
	public String exportMetadataTemplateAsJsonString(long id)
			throws DataNotFoundException, MetadataTemplateException, DataGridException {
		logger.info("exportMetadataTemplateAsJsonString()");

		DataGridTemplate template = this.findById(id);
		if (template == null) {
			logger.warn("no data found for template with id:{}", id);
			throw new DataNotFoundException("no template found");
		}

		logger.info("template:{}", template);
		TimeZone tz = TimeZone.getTimeZone("UTC");
		df.setTimeZone(tz);
		String nowAsISO = df.format(new Date());

		SerializedMetadataTemplate serialized = new SerializedMetadataTemplate();
		serialized.setAccessType(template.getAccessType());
		serialized.setDescription(template.getDescription());
		serialized.setIdentifier(String.valueOf(template.getId()));

		if (template.getCreateTs() != null) {
			serialized.setIso8601CreateDate(df.format(template.getCreateTs()));
		}

		if (template.getModifyTs() != null) {
			serialized.setIso8601ModifyDate(df.format(template.getModifyTs()));
		}

		serialized.setTemplateName(template.getTemplateName());
		serialized.setTemplateOwner(template.getOwner());
		serialized.setVersion(template.getVersion());

		SerializedTemplateField serializedField;
		for (DataGridTemplateField field : template.getFields()) {
			logger.info("field:{}", field);
			serializedField = new SerializedTemplateField();
			serializedField.setAttribute(field.getAttribute());
			serializedField.setFieldOrder(field.getOrder());
			serializedField.setIdentifier(String.valueOf(field.getId()));
			serializedField.setUnit(field.getUnit());
			serializedField.setValue(field.getValue());
			serialized.getTemplateFields().add(serializedField);

		}

		logger.info("build serialized template:{}", serialized);
		try {
			return objectMapper.writeValueAsString(serialized);
		} catch (JsonProcessingException e) {
			logger.error("error serializig metadata template to json", e);
			throw new MetadataTemplateException("error serializing metadata template", e);
		}

	}

	@Override
	public int countAll() {
		int count = templateDao.findAll(DataGridTemplate.class).size();

		return count;
	}

	public TemplateDao getTemplateDao() {
		return templateDao;
	}

	public void setTemplateDao(TemplateDao templateDao) {
		this.templateDao = templateDao;
	}

	public TemplateFieldDao getTemplateFieldDao() {
		return templateFieldDao;
	}

	public void setTemplateFieldDao(TemplateFieldDao templateFieldDao) {
		this.templateFieldDao = templateFieldDao;
	}
}
