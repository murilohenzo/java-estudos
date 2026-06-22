package exemplo02;

import exemplo02.main.Amplifier;
import exemplo02.main.CdPlayer;
import exemplo02.main.HomeTheater;
import exemplo02.main.MusicFacade;

public class Demo {
    public static void main(String[] args) {
        // Configuração do sistema
        Amplifier amplifier = new Amplifier();
        CdPlayer cdPlayer = new CdPlayer();
        HomeTheater homeTheater = new HomeTheater(amplifier, cdPlayer);

        // Criando a fachada para simplificar a interface
        MusicFacade musicFacade = new MusicFacade(homeTheater);

        // Utilizando a fachada para controlar a reprodução de música
        musicFacade.playMusic("Música Relaxante");
        // ... faz outras operações
        musicFacade.stopMusic();
    }
}
