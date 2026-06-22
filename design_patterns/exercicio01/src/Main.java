import exemplo01.domain.enums.CarType;
import exemplo01.domain.enums.Location;
import factories.CarFactory;

public class Main {
    public static void main(String[] args) {
        System.out.println(CarFactory.buildCar(CarType.SMALL, Location.ASIAN));
        System.out.println(CarFactory.buildCar(CarType.SEDAN, Location.USA));
        System.out.println(CarFactory.buildCar(CarType.LUXURY, Location.DEFAULT));
    }
}