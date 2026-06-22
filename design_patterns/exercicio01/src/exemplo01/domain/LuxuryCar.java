package exemplo01.domain;

import exemplo01.domain.enums.CarType;
import exemplo01.domain.enums.Color;
import exemplo01.domain.enums.Location;

public class LuxuryCar extends Car{
    public LuxuryCar(Color color, Location location) {
        super(CarType.LUXURY, color, location);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building luxury car");
    }
}
