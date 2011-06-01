/*******************************************************************************
 * Copyright (c) 2011 Gluster, Inc. <http://www.gluster.com>
 * This file is part of Gluster Management Console.
 *
 * Gluster Management Console is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *  
 * Gluster Management Console is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License
 * for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package com.gluster.storage.management.server.data;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;

/**
 *
 */
public class PersistenceDao<T> {
	private Class<T> type;

	private EntityManager entityManager;

	@PersistenceUnit
	private EntityManagerFactory entityManagerFactory;

	public PersistenceDao(Class<T> type) {
		this.type = type;
	}

	public EntityTransaction startTransaction() {
		EntityTransaction txn = getEntityManager().getTransaction();
		txn.begin();
		return txn;
	}

	private synchronized EntityManager getEntityManager() {
		if (entityManager == null) {
			entityManager = entityManagerFactory.createEntityManager();
		}
		return entityManager;
	}

	public Object getSingleResult(String query) {
		return getEntityManager().createQuery(query).getSingleResult();
	}

	public Object getSingleResult(String queryString, String... params) {
		return createQuery(queryString, params).getSingleResult();
	}

	private Query createQuery(String queryString, String... params) {
		Query query = getEntityManager().createQuery(queryString);
		for (int i = 0; i < params.length; i++) {
			query.setParameter(i + 1, params[i]);
		}
		return query;
	}

	public Object getSingleResultFromSQL(String sqlQuery) {
		return getEntityManager().createNativeQuery(sqlQuery).getSingleResult();
	}
	
	@SuppressWarnings("rawtypes")
	public List findBySQL(String sqlQuery) {
		return getEntityManager().createNativeQuery(sqlQuery).getResultList();
	}

	public T findById(int id) {
		return getEntityManager().find(type, id);
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return getEntityManager().createQuery("select t from " + type.getName() + " t").getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<T> findBy(String whereClause) {
		return getEntityManager().createQuery("select t from " + type.getName() + " t where " + whereClause)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	public List<T> findBy(String whereClause, String... params) {
		return createQuery("select t from " + type.getName() + " t where " + whereClause, params).getResultList();
	}

	public void save(Object obj) {
		getEntityManager().persist(obj);
	}

	public T update(T obj) {
		return getEntityManager().merge(obj);
	}

	public void delete(T obj) {
		getEntityManager().remove(obj);
	}
}
