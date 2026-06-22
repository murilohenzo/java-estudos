package exercicio.domain;

import exercicio.domain.enums.CarType;
import exercicio.domain.enums.Color;

public class SmallCar extends Car {
    public SmallCar(String name, Color color) {
        super(name, color, CarType.SMALL);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building small car");
    }
}
