package factories;

import exemplo01.domain.Car;
import exemplo01.domain.enums.Color;

public class BrasiliaCar extends Car {
    public BrasiliaCar(int horsePower, String fuelSource, Color color) {
        super(horsePower, fuelSource, color);
    }
}
