package ru.sigil.fantasyradio.saved;

import ru.sigil.bassplayerlib.ITrack;

/**
 * Сохранённая mp3. Содержит название, исполнителя, время(в виде строки) и директорию.
 */
public class MP3Entity implements ITrack {
    private String title;
    private String artist;
    private String time;
    private String directory;

    /**
     * @param artist Исполнитель
     * @param title Название
     * @param directory Директория
     * @param time Время (пока не используется)
     */
    public MP3Entity(String artist, String title, String directory, String time)
    {
        setArtist(artist);
        setDirectory(directory);
        setTitle(title);
        setTime(time);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

}
