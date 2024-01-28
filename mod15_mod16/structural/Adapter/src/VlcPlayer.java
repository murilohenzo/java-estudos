import adapters.AdvancedAudioPlayer;

// Implementação da nova interface de reprodução de áudio (Adaptee)
class VlcPlayer implements AdvancedAudioPlayer {
    @Override
    public void playVlc(String fileName) {
        System.out.println("Reproduzindo arquivo Vlc: " + fileName);
    }

    @Override
    public void playMp4(String fileName) {
        throw new RuntimeException("Nao suporta reprodrucao de arquivo Mp4");
    }
}
