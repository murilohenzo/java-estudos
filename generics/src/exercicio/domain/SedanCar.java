package exercicio.domain;

import exercicio.domain.enums.CarType;
import exercicio.domain.enums.Color;

public class SedanCar extends Car {
    public SedanCar(String name, Color color) {
        super(name, color, CarType.SEDAN);
        construct();
    }

    @Override
    protected void construct() {
        System.out.println("Building sedan car");
    }
}
