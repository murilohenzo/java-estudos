package exercicio;

import exercicio.domain.Car;
import exercicio.domain.LuxuryCar;
import exercicio.domain.SedanCar;
import exercicio.domain.SmallCar;
import exercicio.domain.enums.Color;
import exercicio.repository.IGenericCarRepository;
import exercicio.repository.impl.GenericCarRepository;
import exercicio.service.CarService;

import java.util.ArrayList;

public class Main {

    private static IGenericCarRepository repository;

    public static void main(String args[]){
        repository = new GenericCarRepository();
        CarService service = new CarService(repository);

        Car luxuryCar = new LuxuryCar("ferrari", Color.BLACK);
        Car smallCar = new SmallCar("fusquinha", Color.WHITE);
        Car sedanCar = new SedanCar("honda civi", Color.GREEN);

        service.addCar(luxuryCar);
        service.addCar(smallCar);
        service.addCar(sedanCar);

        ArrayList<Car> cars = service.findAll();
        System.out.println(cars);
    }
}
