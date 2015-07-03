package ru.sigil.fantasyradio;

/**
 * Класс, содержащий информацию о потоке
 */
public class RadioEntity {
    private String title;
    private String artist;
    private String station;

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

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

}
