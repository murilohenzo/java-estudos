package generics_project.repository.generic;

import generics_project.domain.Persistence;

import java.util.Collection;
// FACADE PATTERN WITH GENERIC
public interface IGenericRepository <T extends Persistence>{
    void create(T entity);
    void delete (Long value);
    void update(T entity);
    T search(Long value);
    Collection<T> findAll();
}
