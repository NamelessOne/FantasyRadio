package ru.sigil.fantasyradio.utils;

import com.un4seen.bass.BASS;

import java.io.File;

import ru.sigil.fantasyradio.RadioEntity;
import ru.sigil.fantasyradio.saved.MP3Entity;

/**
 * God object. Информацию о проигрываемом файле/потоке.
 * Singleton.
 */
public class PlayerState {
    public static final int AAC16 = 0;
    public static final int MP332 = 1;
    public static final int MP364 = 2;
    public static final int AAC112 = 3;
    public static final int MP396 = 4;
    private static PlayerState instance = new PlayerState();
    private int current_stream = 0;
    private RadioEntity currentRadioEntity;
    private MP3Entity currentMP3Entity;
    private String CurrentArtist = "";
    private String CurrentSong = "";
    private boolean recActive;
    // ----------------------------rec-------------------------
    private String recArtist;
    private String recTitle;
    private String recDirectory;
    private String recTime;
    private String recURL;

    private PlayerState() {
    }

    public static boolean isPlaying() {
        //TODO
        return true;
    }

    public RadioEntity getCurrentRadioEntity() {
        return currentRadioEntity;
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

    public int getCurrent_stream() {
        return current_stream;
    }

    public void setCurrent_stream(int current_stream) {
        this.current_stream = current_stream;
    }

    public boolean isRecActive() {
        return recActive;
    }


    private String getRecArtist() {
        return recArtist;
    }

    public void setRecArtist(String recArtist) {
        this.recArtist = recArtist;
    }

    private String getRecTitle() {
        return recTitle;
    }

    public void setRecTitle(String recTitle) {
        this.recTitle = recTitle;
    }

    private String getRecDirectory() {
        return recDirectory;
    }

    private void setRecDirectory(String recDirectory) {
        this.recDirectory = recDirectory;
    }

    private String getRecTime() {
        return recTime;
    }

    public void setRecTime(String recTime) {
        this.recTime = recTime;
    }

    private String getRecURL() {
        return recURL;
    }

    public void setRecURL(String recURL) {
        this.recURL = recURL;
    }

    public static PlayerState getInstance() {
        return instance;
    }

}
