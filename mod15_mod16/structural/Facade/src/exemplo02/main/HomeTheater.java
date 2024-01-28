package exemplo02.main;

// Subsistema 3: Sistema de HomeTheater
public class HomeTheater {
    private Amplifier amplifier;
    private CdPlayer cdPlayer;

    public HomeTheater(Amplifier amplifier, CdPlayer cdPlayer) {
        this.amplifier = amplifier;
        this.cdPlayer = cdPlayer;
    }

    void startMovie(String movieTitle) {
        amplifier.turnOn();
        amplifier.setVolume(5);
        cdPlayer.turnOn();
        cdPlayer.play(movieTitle);
    }

    void endMovie() {
        amplifier.turnOff();
        cdPlayer.turnOff();
    }
}
