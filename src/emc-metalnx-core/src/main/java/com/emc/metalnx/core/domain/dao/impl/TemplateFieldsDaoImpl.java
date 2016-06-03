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
package com.emc.metalnx.core.domain.dao.impl;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.emc.metalnx.core.domain.dao.TemplateFieldDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;

@SuppressWarnings("unchecked")
@Repository
public class TemplateFieldsDaoImpl extends GenericDaoImpl<DataGridTemplateField, Long> implements TemplateFieldDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public DataGridTemplateField findById(long id) {

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridTemplateField where template_field_id=(:id)");
        q.setParameter("id", id);

        return (DataGridTemplateField) q.uniqueResult();
    }

    @Override
    public boolean modifyById(long id, String attribute, String value, String unit) throws DataGridTemplateAttrException,
            DataGridTemplateValueException, DataGridTemplateUnitException {
        DataGridTemplateField templateField = findById(id);

        if (templateField == null) {
            return false;
        }

        templateField.setAttribute(attribute);
        templateField.setValue(value);
        templateField.setUnit(unit);
        merge(templateField);

        return true;
    }
}
