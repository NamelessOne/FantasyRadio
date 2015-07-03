package ru.sigil.fantasyradio.saved;

public abstract class MP3Saver {
    private static MP3Collection mp3c;

    public static MP3Collection getMp3c() {
        return mp3c;
    }

    public static void setMp3c(MP3Collection mp3c) {
        MP3Saver.mp3c = mp3c;
    }
}
