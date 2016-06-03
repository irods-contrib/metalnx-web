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
package com.emc.metalnx.core.domain.dao.generic;

import java.io.Serializable;
import java.util.List;

import org.hibernate.Query;

@SuppressWarnings("rawtypes")
public interface GenericDao<T, id extends Serializable> {

	/**
	 * Persists a new entry on the database.
	 * @param entity
	 */
	public id save(T entity);
	 
	/**
	 * Updates an existing entry on the database.
	 * @param entity
	 */
    public void merge(T entity);
 
    /**
     * Deletes an entry from the database.
     * @param entity
     */
    public void delete(T entity);
 
    /**
     * Returns a list of entities of class T matching the query.
     * @param query
     * @return
     */
    public List<T> findMany(Query query);
 
    /**
     * Returns one single entity of class T matching the query.
     * @param query
     * @return
     */
    public T findOne(Query query);
    
    /**
     * Returns all the entries for the class.
     * @param clazz
     * @return
     */
	public List<T> findAll(Class clazz);
 
	/**
	 * Find an entry by its ID.
	 * @param clazz
	 * @param id
	 * @return
	 */
    public T findByID(Class clazz, Long id);
}
