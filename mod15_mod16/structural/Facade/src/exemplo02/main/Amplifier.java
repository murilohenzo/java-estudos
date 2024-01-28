package exemplo02.main;

// Subsistema 1: Amplificador
public class Amplifier {
    void turnOn() {
        System.out.println("Amplificador ligado");
    }

    void setVolume(int volume) {
        System.out.println("Volume ajustado para " + volume);
    }

    void turnOff() {
        System.out.println("Amplificador desligado");
    }
}
