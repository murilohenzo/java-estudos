package exemplo02.main;

// Facade: Fornece uma interface simplificada para o cliente
public class MusicFacade {
    private HomeTheater homeTheater;

    public MusicFacade(HomeTheater homeTheater) {
        this.homeTheater = homeTheater;
    }

    // Método de interface simplificada para iniciar a reprodução de música
    public void playMusic(String musicTitle) {
        homeTheater.startMovie(musicTitle);
    }

    // Método de interface simplificada para encerrar a reprodução de música
    public void stopMusic() {
        homeTheater.endMovie();
    }
}
