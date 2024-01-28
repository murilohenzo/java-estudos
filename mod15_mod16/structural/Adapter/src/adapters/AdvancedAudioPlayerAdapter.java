package adapters;

// Adaptador que converte a interface de AdvancedAudioPlayer para MediaPlayer
public class AdvancedAudioPlayerAdapter implements MediaPlayer {

    private AdvancedAudioPlayer  advancedAudioPlayer;

    public AdvancedAudioPlayerAdapter(AdvancedAudioPlayer advancedAudioPlayer) {
        this.advancedAudioPlayer = advancedAudioPlayer;
    }

    @Override
    public void playAudio(String audioType, String fileName) {
        if (audioType.equalsIgnoreCase("vlc")) {
            advancedAudioPlayer.playVlc(fileName);
        } else if (audioType.equalsIgnoreCase("mp4")) {
            // Podemos converter a reprodução de Mp4 para algo compatível com MediaPlayer, se necessário.
            System.out.println("Reproduzindo arquivo Mp4 (convertido): " + fileName);
        }

    }

    @Override
    public void playVideo(String videoType, String fileName) {
        throw new RuntimeException("Não suporta reprodução de vídeo no contexto deste exemplo");
    }
}
