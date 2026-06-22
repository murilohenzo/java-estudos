package generics_project.repository.generic.implementation;

import generics_project.domain.Persistence;
import generics_project.repository.generic.IGenericRepository;
import generics_project.singleton.SingletonMap;

import java.util.*;

// facade pattern
public abstract class GenericRepository<T extends Persistence> implements IGenericRepository<T> {


    private SingletonMap singletonMap;
    public abstract Class<T> getClassType();

    // template method pattern
    public abstract void updateEntity(T existsEntity, T entity);

    public GenericRepository() {
        this.singletonMap = SingletonMap.getInstance();
    }

    @Override
    public void create(T entity) {
        Map<Long, T> tMap = (Map<Long, T>) this.singletonMap.getMap().get(getClassType());
        tMap.put(entity.getCode(), entity);
    }

    @Override
    public void delete(Long value) {
        Map<Long, T> tMap = (Map<Long, T>) this.singletonMap.getMap().get(getClassType());
        T entity = tMap.get(value);
        if (Objects.nonNull(entity)) {
            tMap.remove(value, entity);
        }
    }

    @Override
    public void update(T entity) {
        Map<Long, T> tMap = (Map<Long, T>) this.singletonMap.getMap().get(getClassType());
        T existsEntity = tMap.get(entity.getCode());
        if (Objects.nonNull(existsEntity)) {
            updateEntity(existsEntity, entity);
        }
    }

    @Override
    public T search(Long value) {
        Map<Long, T> tMap = (Map<Long, T>) this.singletonMap.getMap().get(getClassType());
        return tMap.get(value);
    }

    @Override
    public Collection<T> findAll() {
        Map<Long, T> tMap = (Map<Long, T>) this.singletonMap.getMap().get(getClassType());
        return tMap.values();
    }
}
