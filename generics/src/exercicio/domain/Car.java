package exercicio.domain;

import exercicio.domain.enums.CarType;
import exercicio.domain.enums.Color;

public abstract class Car {
    private String name;
    private Color color;
    private CarType carType;

    public Car() {}

    public Car(String name, Color color, CarType carType) {
        this.name = name;
        this.color = color;
        this.carType = carType;
    }

    protected abstract void construct();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public CarType getCarType() {
        return carType;
    }

    public void setCarType(CarType carType) {
        this.carType = carType;
    }

    @Override
    public String toString() {
        return "Car{" +
                "name='" + name + '\'' +
                ", color=" + color +
                ", carType=" + carType +
                '}';
    }
}
