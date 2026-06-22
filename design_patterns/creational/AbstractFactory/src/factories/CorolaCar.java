package factories;

import exemplo01.domain.Car;
import exemplo01.domain.enums.Color;

public class CorolaCar extends Car {
    public CorolaCar(int horsePower, String fuelSource, Color color) {
        super(horsePower, fuelSource, color);
    }
}
