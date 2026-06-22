package exemplo01.domain;

import exemplo01.domain.enums.CarType;
import exemplo01.domain.enums.Color;
import exemplo01.domain.enums.Location;

public class SedanCar extends Car{
    public SedanCar(Color color, Location location) {
        super(CarType.SEDAN, color, location);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building sedan car");
    }
}
