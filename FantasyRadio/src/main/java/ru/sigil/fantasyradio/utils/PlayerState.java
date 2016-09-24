package ru.sigil.fantasyradio.utils;

import ru.sigil.fantasyradio.RadioEntity;
import ru.sigil.fantasyradio.saved.MP3Entity;

/**
 * God object. Информацию о проигрываемом файле/потоке.
 * Singleton.
 */
public class PlayerState {
    private static PlayerState instance = new PlayerState();
    private RadioEntity currentRadioEntity;
    private MP3Entity currentMP3Entity;
    private String CurrentArtist = "";
    private String CurrentSong = "";

    private PlayerState() {
    }

    public static boolean isPlaying() {
        //TODO
        return true;
    }

    public void setCurrentRadioEntity(RadioEntity newCurrentEntity) {
        currentRadioEntity = newCurrentEntity;
        if (newCurrentEntity != null) {
            CurrentArtist = currentRadioEntity.getArtist();
            CurrentSong = currentRadioEntity.getTitle();
            currentMP3Entity = null;
        }
        ProgramNotification.changeSongName(getCurrentSong(),
                getCurrentArtist());
    }

    public MP3Entity getCurrentMP3Entity() {
        return currentMP3Entity;
    }

    public void setCurrentMP3Entity(MP3Entity newCurrentMP3Entity) {
        currentRadioEntity = null;

        currentMP3Entity = newCurrentMP3Entity;
        if (newCurrentMP3Entity != null) {
            CurrentArtist = currentMP3Entity.getArtist();
            CurrentSong = currentMP3Entity.getTitle();
        }
        ProgramNotification.changeSongName(getCurrentSong(),
                getCurrentArtist());// !!!!
    }

    public String getCurrentArtist() {
        return CurrentArtist;
    }

    public String getCurrentSong() {
        return CurrentSong;
    }

    public void setRecArtist(String recArtist) {
    }

    public void setRecTitle(String recTitle) {
    }

    public void setRecTime(String recTime) {
    }

    public void setRecURL(String recURL) {
    }

    public static PlayerState getInstance() {
        return instance;
    }

}
