 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.irods;

import com.emc.metalnx.core.domain.dao.TemplateFieldDao;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;
import com.emc.metalnx.services.interfaces.TemplateFieldService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TemplateFieldServiceImpl implements TemplateFieldService {

    @Autowired
    private TemplateFieldDao templateFieldDao;

    private static final Logger logger = LoggerFactory.getLogger(TemplateFieldServiceImpl.class);

    @Override
    public DataGridTemplateField findById(long id) {
        logger.info("Find template field by ID {}", id);
        return templateFieldDao.findById(id);
    }

    @Override
    public long createTemplateField(DataGridTemplateField dataGridTemplateField) {
        if (dataGridTemplateField == null) {
            return 0;
        }

        logger.info("Creating template field {}, {}, {} for template {}", dataGridTemplateField.getAttribute(), dataGridTemplateField.getValue(),
                dataGridTemplateField.getUnit(), dataGridTemplateField.getTemplate().getTemplateName());

        return templateFieldDao.save(dataGridTemplateField);
    }

    @Override
    public List<DataGridTemplateField> findAll() {
        logger.info("Find all template fields.");
        return templateFieldDao.findAll(DataGridTemplateField.class);
    }

    @Override
    public boolean deleteTemplateField(DataGridTemplateField dataGridTemplateField) {
        if (dataGridTemplateField == null) {
            return false;
        }

        logger.info("Delete template field {}", dataGridTemplateField.getAttribute(), dataGridTemplateField.getValue(),
                dataGridTemplateField.getUnit(), dataGridTemplateField.getTemplate().getTemplateName());

        templateFieldDao.delete(dataGridTemplateField);

        return true;
    }

    @Override
    public boolean modifyTemplateField(long id, String attribute, String value, String unit) throws DataGridTemplateAttrException,
            DataGridTemplateValueException, DataGridTemplateUnitException {
        logger.info("Modify template field by id {}", id);
        return templateFieldDao.modifyById(id, attribute, value, unit);
    }
}
