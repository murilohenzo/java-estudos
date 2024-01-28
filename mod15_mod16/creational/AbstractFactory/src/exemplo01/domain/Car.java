package exemplo01.domain;

import exemplo01.domain.enums.Color;

public abstract class Car {
    private final int horsePower;
    private final String fuelSource;
    private final Color color;

    public Car(int horsePower, String fuelSource, Color color) {
        this.horsePower = horsePower;
        this.fuelSource = fuelSource;
        this.color = color;
    }

    public void startEngine(){
        System.out.println(getClass().getSimpleName());
        System.out.println("The " + fuelSource + " engine has been started, and is ready to utilize " + horsePower + " horsepowers.n" );
    }
    public void clean(){
        System.out.println("Car has been cleaned, and the " + color.toString().toLowerCase() + " color shines");
    }
    public void mechanicCheck(){
        System.out.println("Car has been checked by the mechanic. Everything looks good!");
    }
    public void fuelCar(){
        System.out.println("Car is being filled with " + fuelSource.toLowerCase());
    }


}
