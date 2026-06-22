package exercicio.repository;

import exercicio.domain.Car;

import java.util.ArrayList;

public interface IGenericCarRepository<T extends Car> {

    void create(T entity);
    ArrayList<T> findAll();
}
