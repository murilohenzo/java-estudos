package exercicio.domain;

import exercicio.domain.enums.CarType;
import exercicio.domain.enums.Color;

public class LuxuryCar extends Car {
    public LuxuryCar(String name, Color color) {
        super(name, color, CarType.LUXURY);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building luxury car");
    }
}
