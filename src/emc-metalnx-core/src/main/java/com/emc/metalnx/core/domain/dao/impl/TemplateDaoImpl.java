 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

package com.emc.metalnx.core.domain.dao.impl;

import com.emc.metalnx.core.domain.dao.TemplateDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
@Repository
public class TemplateDaoImpl extends GenericDaoImpl<DataGridTemplate, Long>implements TemplateDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public long getTemplateId(String templateName) {
        DataGridTemplate dataGridTemplate = this.findByName(templateName);

        if (dataGridTemplate == null) {
            return -1;
        }

        return dataGridTemplate.getId();
    }

    @Override
    public DataGridTemplate findById(long id) {

        Query q = this.sessionFactory.getCurrentSession().createQuery("from DataGridTemplate where template_id=(:id)");
        q.setParameter("id", id);

        return (DataGridTemplate) q.uniqueResult();
    }

    @Override
    public DataGridTemplate findByName(String templateName) {

        Query q = this.sessionFactory.getCurrentSession()
                .createQuery("from DataGridTemplate where template_name = :templateName");
        q.setString("templateName", templateName);

        return (DataGridTemplate) q.uniqueResult();
    }

    @Override
    public boolean deleteById(long id) {
        DataGridTemplate dataGridTemplate = this.findById(id);

        if (dataGridTemplate == null) {
            return false;
        }

        this.delete(dataGridTemplate);

        return true;
    }

    @Override
    public List<DataGridTemplate> findByQueryString(String query) {
        Query q = this.sessionFactory.getCurrentSession()
                .createQuery("from DataGridTemplate where template_name like :templateName");

        q.setParameter("templateName", "%" + query + "%");

        // Returning results
        return q.list();
    }

    @Override
    public List<DataGridTemplateField> listTemplateFields(Long id) {
        Query q = this.sessionFactory.getCurrentSession()
                .createQuery("from DataGridTemplateField where template_id = :templateID");

        q.setParameter("templateID", id);

        // Returning results
        return q.list();
    }

    @Override
    public List<DataGridTemplateField> listTemplateFields(String template) {
        long id = this.getTemplateId(template);

        if (id > 0) {
            return this.listTemplateFields(id);
        }

        return new ArrayList<DataGridTemplateField>();
    }

    @Override
    public List<DataGridTemplate> listPublicTemplates() {
        Query q = this.sessionFactory.getCurrentSession()
                .createQuery("from DataGridTemplate where access_type = :accessType");

        q.setParameter("accessType", "system");

        // Returning results
        return q.list();
    }

    @Override
    public List<DataGridTemplate> listPrivateTemplatesByUser(String user) {
        Query q = this.sessionFactory.getCurrentSession()
                .createQuery("from DataGridTemplate where access_type = :accessType and owner = :owner");

        q.setParameter("accessType", "private");
        q.setParameter("owner", user);

        // Returning results
        return q.list();
    }

    @Override
    public void merge(DataGridTemplate template) {
        if (template.isModified()) {
            template.setVersion(template.getVersion() + 1);
        }

        super.merge(template);
    }
}
