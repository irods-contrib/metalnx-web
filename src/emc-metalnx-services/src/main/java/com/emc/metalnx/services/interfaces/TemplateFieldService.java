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

package com.emc.metalnx.services.interfaces;

import java.util.List;

import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;

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
