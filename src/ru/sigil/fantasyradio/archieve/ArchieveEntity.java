package ru.sigil.fantasyradio.archieve;

public class ArchieveEntity {
    private String URL;
    private String Name;
    private String Time;

    public String getURL() {
        return URL;
    }

    public void setURL(String uRL) {
        URL = uRL;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getFileName() {
        int x = this.getURL().lastIndexOf('/');
        return this.getURL().substring(x + 1);
    }
}
