package exercicio.service;

import exercicio.domain.Car;
import exercicio.repository.IGenericCarRepository;

import java.util.ArrayList;

public class CarService {
    private final IGenericCarRepository repository;

    public CarService(IGenericCarRepository repository) {
        this.repository = repository;
    }

    public void addCar(Car car) {
        this.repository.create(car);
    }

    public ArrayList<Car> findAll() {
        return this.repository.findAll();
    }
}
