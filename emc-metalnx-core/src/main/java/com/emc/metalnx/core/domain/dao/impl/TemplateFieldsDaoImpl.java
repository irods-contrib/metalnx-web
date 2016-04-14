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
