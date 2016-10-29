package ru.sigil.fantasyradio.saved;

/**
 * Сохранённая mp3. Содержит название, исполнителя, время(в виде строки) и директорию.
 */
public class MP3Entity {
    private String title;
    private String artist;
    private String time;
    private String directory;

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
