package factories;

import exemplo01.domain.Car;

public interface IFactory {
    default Car create(String request) {
        Car car = retrieveCar(request);
        return prepareCar(car);
    }
    private Car prepareCar(Car car){
        car.clean();
        car.mechanicCheck();
        car.fuelCar();
        return car;
    }
    Car retrieveCar(String request);
}
