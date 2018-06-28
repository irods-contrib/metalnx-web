 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 


package com.emc.metalnx.services.interfaces;

import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;

import java.util.List;

public interface TemplateFieldService {

    /**
     * Finds a template field by id
     * 
     * @param id
     *            id of the template field to be found
     * @return DataGridTemplateField object if found. Null, otherwise.
     */
    public DataGridTemplateField findById(long id);

    /**
     * Creates a template field into the database
     * 
     * @param dataGridTemplateField
     *            template field to be saved into the database
     * @return The id of the template field just created
     */
    public long createTemplateField(DataGridTemplateField dataGridTemplateField);

    /**
     * Lists all template fields existing in the database.
     * 
     * @return List of template fields
     */
    public List<DataGridTemplateField> findAll();

    /**
     * Deletes a template field from the database
     * 
     * @param dataGridTemplate
     *            template field object to be removed
     * @return True, if the template field was successfully removed. False, otherwise.
     */
    public boolean deleteTemplateField(DataGridTemplateField dataGridTemplateField);

    /**
     * Modifies a template field from the database based on its id
     * 
     * @param id
     *            id of the template field to be removed
     * @return True, if the template field was successfully modified. False, otherwise.
     * @throws DataGridTemplateUnitException
     * @throws DataGridTemplateValueException
     * @throws DataGridTemplateAttrException
     */
    boolean modifyTemplateField(long id, String attribute, String value, String unit) throws DataGridTemplateAttrException,
            DataGridTemplateValueException, DataGridTemplateUnitException;

}
