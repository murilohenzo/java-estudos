package exercicio.repository.impl;
import exercicio.domain.Car;
import exercicio.repository.IGenericCarRepository;

import java.util.ArrayList;

public class GenericCarRepository<T extends Car> implements IGenericCarRepository<T>{
    private final ArrayList<T> repository;

    public GenericCarRepository() {
        this.repository = new ArrayList<>();
    }

    @Override
    public void create(T entity) {
        this.repository.add(entity);
    }

    @Override
    public ArrayList<T> findAll() {
        return this.repository;
    }
}
