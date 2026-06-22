package adapters;

// Interface para o player de midia que o cliente espera
public interface MediaPlayer {
    void playAudio(String audioType, String fileName);
    void playVideo(String videoType, String fileName);
}
