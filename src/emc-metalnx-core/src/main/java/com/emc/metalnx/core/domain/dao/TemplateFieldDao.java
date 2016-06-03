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
