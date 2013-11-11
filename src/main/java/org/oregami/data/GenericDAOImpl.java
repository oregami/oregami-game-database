package org.oregami.data;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.oregami.entities.BaseEntity;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.persist.Transactional;

public abstract class GenericDAOImpl<E extends BaseEntity, P> implements
		GenericDAO<E, P> {

	private final Provider<EntityManager> emf;
	
	@Inject
	public GenericDAOImpl(Provider<EntityManager> emf) {
		this.emf=emf;
	}

	Class<E> entityClass;

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public P save(E entity) {
		emf.get().persist(entity);
		return (P) entity.getId();
	}

	@Override
	public E findOne(P id) {
		return emf.get().find(getEntityClass(), id);
	}

	@Override
	@Transactional
	public void update(E entity) {
		emf.get().merge(entity);
	}

	@Override
	public void delete(E entity) {
		emf.get().remove(entity);
	}

	@Override
	public EntityManager getEntityManager() {
		return emf.get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Class<E> getEntityClass() {
		if (entityClass == null) {
			Type type = getClass().getGenericSuperclass();
			if (type instanceof ParameterizedType) {
				ParameterizedType paramType = (ParameterizedType) type;

				entityClass = (Class<E>) paramType.getActualTypeArguments()[0];

			} else {
				throw new IllegalArgumentException(
						"Could not guess entity class by reflection");
			}
		}
		return entityClass;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<E> findAll() {
		return this.emf.get().createNamedQuery(
				getEntityClass().getSimpleName() + ".GetAll").getResultList();
	}
	
	
	public EntityTransaction getTransaction() {
		return getEntityManager().getTransaction();
	}

}