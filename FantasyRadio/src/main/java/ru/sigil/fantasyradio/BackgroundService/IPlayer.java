package ru.sigil.fantasyradio.BackgroundService;

import ru.sigil.fantasyradio.saved.MP3Saver;

/**
 * Created by NamelessOne
 * on 17.09.2016.
 */
public interface IPlayer {
    String[] RESERVED_CHARS = {"|", "\\", "?", "*", "<", "\"",
            ":", ">", "+", "[", "]", "/", "'", "%"};
    void play(String URL, Bitrate bitrate);
    void playAAC(String URL, Bitrate bitrate);
    void playFile(String file);
    void stop();
    void addEventListener(IPlayerEventListener listener);
    void removeEventListener(IPlayerEventListener listener);
    void removeAllListeners();
    String currentTitle();
    String currentArtist();
    Bitrate currentBitrate();
    PlayState currentState();
    void setBitrate(Bitrate bitrate);
    boolean isRecActive();
    void setChan(int chan);
    int getChan();
    long getFileLength();
    float getVolume();
    void rewind(int offset);
    void rec(boolean isActive);
    MP3Saver getMp3Saver();
}