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

import java.util.List;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.emc.metalnx.core.domain.dao.UserProfileDao;
import com.emc.metalnx.core.domain.dao.generic.GenericDaoImpl;
import com.emc.metalnx.core.domain.entity.UserProfile;

@SuppressWarnings("unchecked")
@Repository
@Transactional
public class UserProfileDaoImpl extends GenericDaoImpl<UserProfile, Long>
		implements UserProfileDao {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public List<UserProfile> findByQueryString(String query) {
		Query q = sessionFactory
				.getCurrentSession()
				.createQuery(
						"from UserProfile where profile_name like :name or description like :description ");

		q.setParameter("name", "%" + query + "%");
		q.setParameter("description", "%" + query + "%");

		// Returning results
		return q.list();
	}
	
}
