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

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.emc.metalnx.core.domain.dao.GroupDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridGroup;

@SuppressWarnings("unchecked")
@Repository
public class GroupDaoImpl extends GenericDaoImpl<DataGridGroup, Long> implements GroupDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<DataGridGroup> findByGroupname(String groupname) {

        List<DataGridGroup> dataGridGroups = null;
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where groupname = :groupname");
        q.setString("groupname", groupname);

        dataGridGroups = q.list();

        return dataGridGroups;
    }

    @Override
    public DataGridGroup findByGroupnameAndZone(String groupname, String zone) {

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where groupname = :groupname and additional_info = :zone");
        q.setString("groupname", groupname);
        q.setParameter("zone", zone);

        return (DataGridGroup) q.uniqueResult();
    }

    @Override
    public boolean deleteByGroupname(String groupname) {

        boolean operationResult = true;

        try {
            List<DataGridGroup> dataGridGroups = findByGroupname(groupname);

            for (DataGridGroup dataGridGroup : dataGridGroups) {
                delete(dataGridGroup);
            }
        }
        catch (Exception e) {
            operationResult = false;
        }

        return operationResult;
    }

    @Override
    public List<DataGridGroup> findByQueryString(String query) {
        Query q = sessionFactory.getCurrentSession().createQuery(
                "from DataGridGroup where groupname like :groupname or additional_info like :additional_info");

        q.setParameter("groupname", "%" + query + "%");
        q.setParameter("additional_info", "%" + query + "%");

        // Returning results
        return q.list();
    }

    @Override
    public List<DataGridGroup> findByDataGridIdList(String[] ids) {
        // Checking if the input ID list is empty
        if (ids == null || ids.length == 0) {
            return new ArrayList<DataGridGroup>();
        }

        Long[] idsLong = convertStringsToLongs(ids);
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where data_grid_id in (:ids)");
        q.setParameterList("ids", idsLong);

        return q.list();

    }

    @Override
    public List<DataGridGroup> findByGroupNameList(String[] groupNames) {
        // Checking if the input ID list is empty
        if (groupNames == null || groupNames.length == 0) {
            return new ArrayList<DataGridGroup>();
        }

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where groupname in (:groupNames)");
        q.setParameterList("groupNames", groupNames);

        return q.list();
    }

    @Override
    public boolean deleteByDataGridGroupId(long id) {
        DataGridGroup dataGridGroup = findByDataGridId(id);

        if (dataGridGroup != null) {
            delete(dataGridGroup);
            return true;
        }

        return false;
    }

    @Override
    public DataGridGroup findByDataGridId(long id) {
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where data_grid_id=(:id)");
        q.setParameter("id", id);

        List<DataGridGroup> groups = q.list();
        return groups.size() > 0 ? groups.get(0) : null;
    }

    @Override
    public List<DataGridGroup> findByIdList(Long[] ids) {
        if (ids == null || ids.length == 0) {
            return new ArrayList<DataGridGroup>();
        }

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridGroup where id in (:ids)");
        q.setParameterList("ids", ids);

        return q.list();
    }

    /**
     * Converts an array of strings into an array of longs.
     *
     * @param strArray
     *            array of strings to be converted
     * @return array of longs
     */
    private Long[] convertStringsToLongs(String[] strArray) {
        Long[] intArray = new Long[strArray.length];

        for (int i = 0; i < strArray.length; i++) {
            intArray[i] = Long.valueOf(strArray[i]);
        }

        return intArray;
    }

}
