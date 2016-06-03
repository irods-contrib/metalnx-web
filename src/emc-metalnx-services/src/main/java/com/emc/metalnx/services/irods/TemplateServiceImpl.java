/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.services.irods;

import java.io.InputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.emc.com.emc.metalnx.core.xml.MlxMetadataAVU;
import com.emc.com.emc.metalnx.core.xml.MlxMetadataTemplate;
import com.emc.com.emc.metalnx.core.xml.MlxMetadataTemplates;
import com.emc.metalnx.core.domain.dao.TemplateDao;
import com.emc.metalnx.core.domain.dao.TemplateFieldDao;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;
import com.emc.metalnx.core.domain.exceptions.DataGridTooLongTemplateNameException;
import com.emc.metalnx.services.interfaces.TemplateService;

@Service
@Transactional
public class TemplateServiceImpl implements TemplateService {

    @Autowired
    private TemplateDao templateDao;

    @Autowired
    private TemplateFieldDao templateFieldDao;

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
         * we need to remove all template fields existing in a template before
         * removing the Template itself
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
    public boolean importXmlMetadataTemplate(InputStream inStream, String owner, String prefix, String suffix) throws JAXBException,
            DataGridTooLongTemplateNameException, DataGridTemplateAttrException, DataGridTemplateValueException, DataGridTemplateUnitException {

        JAXBContext jaxbContext = JAXBContext.newInstance(MlxMetadataTemplates.class);
        Unmarshaller un = jaxbContext.createUnmarshaller();
        MlxMetadataTemplates ts = (MlxMetadataTemplates) un.unmarshal(inStream);

        boolean result = true;

        for (MlxMetadataTemplate t : ts.getTemplates()) {

            String newTemplateName = String.format("%s%s%s", prefix, t.getName(), suffix);

            if (findByName(newTemplateName) != null) {
                logger.info("Template with name {} already exists on the database", newTemplateName);
                result = false;
                continue;
            }

            DataGridTemplate nt = new DataGridTemplate();
            nt.setTemplateName(newTemplateName);
            nt.setDescription(t.getDescription());
            nt.setUsageInformation(t.getUsageInfo());
            nt.setAccessType(t.getAccessType());
            nt.setOwner(owner);

            nt.setFields(new HashSet<DataGridTemplateField>());
            long tid = createTemplate(nt);
            nt.setId(tid);

            for (MlxMetadataAVU a : t.getMetadatas()) {
                DataGridTemplateField na = new DataGridTemplateField();
                na.setAttribute(a.getAttribute());
                na.setValue(a.getValue());
                na.setUnit(a.getUnit());
                na.setTemplate(nt);
                templateFieldDao.save(na);
            }
        }
        return result;
    }

    @Override
    public MlxMetadataTemplate mapDataGridTemplateToXml(DataGridTemplate template) {
        // Mapping DB entity to XML entity
        MlxMetadataTemplate t = new MlxMetadataTemplate();
        t.setName(template.getTemplateName());
        t.setDescription(template.getDescription());
        t.setUsageInfo(template.getUsageInformation());
        t.setAccessType(template.getAccessType());

        for (DataGridTemplateField field : template.getFields()) {
            MlxMetadataAVU avu = new MlxMetadataAVU();
            avu.setAttribute(field.getAttribute());
            avu.setValue(field.getValue());
            avu.setUnit(field.getUnit());
            t.getMetadatas().add(avu);
        }

        return t;
    }

    @Override
    public int countAll() {
        int count = templateDao.findAll(DataGridTemplate.class).size();

        return count;
    }
}
