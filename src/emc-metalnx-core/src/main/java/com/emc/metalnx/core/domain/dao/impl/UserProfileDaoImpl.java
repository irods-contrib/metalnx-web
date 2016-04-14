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
