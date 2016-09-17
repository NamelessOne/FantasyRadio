package ru.sigil.fantasyradio.BackgroundService;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
public interface IPlayer {
    void play(String URL);
    void playAAC(String URL);
    void addEventListener(IPlayerEventListener listener);
    void removeEventListener(IPlayerEventListener listener);
    void removeAllListeners();
    String currentTitle();
    String currentArtist();
    Bitrate currentBitrate();
    PlayState currentState();
    boolean isRecActive();
    void setBitrate(Bitrate bitrate);
}
