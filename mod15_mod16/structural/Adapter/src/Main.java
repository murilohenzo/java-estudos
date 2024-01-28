import adapters.AdvancedAudioPlayer;
import adapters.AdvancedAudioPlayerAdapter;
import adapters.MediaPlayer;

public class Main {
    public static void main(String[] args) {
        // Utilizando o player de mídia existente
        MediaPlayer mediaPlayer = new LegacyMediaPlayer();
        mediaPlayer.playAudio("mp3", "musica.mp3");
        mediaPlayer.playVideo("mp4", "filme.mp4");

        // Utilizando o novo player de áudio através do adaptador
        AdvancedAudioPlayer vlcPlayer = new VlcPlayer();
        MediaPlayer advancedAdapter = new AdvancedAudioPlayerAdapter(vlcPlayer);
        advancedAdapter.playAudio("vlc", "audio.vlc");
    }
}