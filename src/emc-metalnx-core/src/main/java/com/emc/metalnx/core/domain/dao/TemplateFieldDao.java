package com.emc.metalnx.core.domain.dao;

import com.emc.metalnx.core.domain.dao.generic.GenericDao;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;

public interface TemplateFieldDao extends GenericDao<DataGridTemplateField, Long> {

    /**
     * Find a template field by its id
     *
     * @param id
     *            template field id
     * @return Template if the id exists. Null, if the id was not found.
     */
    DataGridTemplateField findById(long id);

    /**
     * Modify a template field by its id
     *
     * @param id
     *            id of the template field to be removed
     * @return true if the template field was successfully modified. False, otherwise.
     * @throws DataGridTemplateAttrException
     * @throws DataGridTemplateValueException
     * @throws DataGridTemplateUnitException
     */
    boolean modifyById(long id, String attribute, String value, String unit) throws DataGridTemplateAttrException, DataGridTemplateValueException,
            DataGridTemplateUnitException;
}
