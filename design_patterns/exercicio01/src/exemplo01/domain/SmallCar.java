package exemplo01.domain;

import exemplo01.domain.enums.CarType;
import exemplo01.domain.enums.Color;
import exemplo01.domain.enums.Location;

public class SmallCar extends Car{
    public SmallCar(Color color, Location location) {
        super(CarType.SMALL, color, location);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building small car");
    }
}
