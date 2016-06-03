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
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.DataGridUser;

@SuppressWarnings("unchecked")
@Repository
@Transactional
public class UserDaoImpl extends GenericDaoImpl<DataGridUser, Long> implements UserDao {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public List<DataGridUser> findByUsername(String username) {

        List<DataGridUser> users = null;
        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUser where username = :username");
        q.setString("username", username);

        users = q.list();

        return users;
    }

    @Override
    public DataGridUser findByUsernameAndZone(String username, String zone) {

        List<DataGridUser> users = null;

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUser where username = :username and additional_info = :zone");
        q.setString("username", username);
        q.setString("zone", zone);

        users = q.list();
        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public boolean deleteByUsername(String username) {
        List<DataGridUser> users = findByUsername(username);
        for (DataGridUser user : users) {
            delete(user);
        }
        return true;
    }

    @Override
    public List<DataGridUser> findByQueryString(String query) {
        Query q = sessionFactory.getCurrentSession().createQuery(
                "from DataGridUser where username like :username or additional_info like :additional_info "
                        + "or first_name like :first_name or last_name like :last_name " + "or email like :email order by username");

        q.setParameter("username", "%" + query + "%");
        q.setParameter("additional_info", "%" + query + "%");
        q.setParameter("first_name", "%" + query + "%");
        q.setParameter("last_name", "%" + query + "%");
        q.setParameter("email", "%" + query + "%");

        // Returning results
        return q.list();
    }

    @Override
    public List<DataGridUser> findByDataGridIdList(String[] ids) {
        List<DataGridUser> result = new ArrayList<DataGridUser>();

        if (ids != null) {
            int i = 0;
            Integer ids_int[] = new Integer[ids.length];

            for (String id_str : ids) {
                ids_int[i++] = Integer.parseInt(id_str);
            }

            // Checking if the input ID list is empty
            if (ids_int != null) {
                Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUser where data_grid_id in (:ids)");
                q.setParameterList("ids", ids_int);
                result = q.list();
            }
        }

        // If the input list is null, the method returns null
        return result;
    }

    @Override
    public DataGridUser findByDataGridId(long id) {

        Query q = sessionFactory.getCurrentSession().createQuery("from DataGridUser where data_grid_id=(:id)");
        q.setParameter("id", id);

        List<DataGridUser> users = q.list();

        return users.size() > 0 ? users.get(0) : null;
    }

    @Override
    public boolean deleteByDataGridId(long id) {
        DataGridUser user = findByDataGridId(id);
        if (user != null) {
            delete(user);
        }
        return false;
    }

}
