package factories;

import exemplo01.domain.Car;
import factories.CorolaCar;
import exemplo01.domain.enums.Color;
import factories.IFactory;

public class ContratosFactory implements IFactory {

    private String request;

    public ContratosFactory(String request) {
        this.request = request;
    }

    @Override
    public Car retrieveCar(String request) {
        if ("A".equals(request)) {
            return new CorolaCar(100, "cheio", Color.BLACK);
        } else {
            return null;
        }
    }
}
