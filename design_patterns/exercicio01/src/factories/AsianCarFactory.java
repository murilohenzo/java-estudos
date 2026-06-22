package factories;

import exemplo01.domain.Car;
import exemplo01.domain.LuxuryCar;
import exemplo01.domain.SedanCar;
import exemplo01.domain.SmallCar;
import exemplo01.domain.enums.CarType;
import exemplo01.domain.enums.Color;
import exemplo01.domain.enums.Location;

public class AsianCarFactory {
    public static Car buildCar(CarType type) {
        Car car = null;
        switch (type) {
            case SMALL:
                car = new SmallCar(Color.BLACK, Location.ASIAN);
                break;
            case SEDAN:
                car = new SedanCar(Color.BLACK, Location.ASIAN);
                break;
            case LUXURY:
                car = new LuxuryCar(Color.WHITE, Location.ASIAN);
                break;
            default:
                break;
        }
        return car;
    }
}
