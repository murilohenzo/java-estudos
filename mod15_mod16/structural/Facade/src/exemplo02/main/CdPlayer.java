package exemplo02.main;

// Subsistema 2: Reprodutor de CD
public class CdPlayer {
    void turnOn() {
        System.out.println("Reprodutor de CD ligado");
    }

    void play(String cdTitle) {
        System.out.println("Reproduzindo CD: " + cdTitle);
    }

    void turnOff() {
        System.out.println("Reprodutor de CD desligado");
    }
}