import adapters.MediaPlayer;

// Classe que representa o player de midia existente
public class LegacyMediaPlayer implements MediaPlayer {
    @Override
    public void playAudio(String audioType, String fileName) {
        System.out.println("Reproduzindo audio: " + fileName);
    }

    @Override
    public void playVideo(String videoType, String fileName) {
        System.out.println("Reproduzindo video: " + fileName);
    }
}
