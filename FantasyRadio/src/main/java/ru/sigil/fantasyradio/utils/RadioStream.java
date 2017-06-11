package ru.sigil.fantasyradio.utils;

import ru.sigil.bassplayerlib.IRadioStream;

/**
 * Created by
 * namelessone on 11.03.17.
 */

public class RadioStream implements IRadioStream {

    private Bitrate bitrate;
    private String streamURl = "";

    public RadioStream(Bitrate bitrate, String streamURl) {
        if(streamURl==null)
            throw new IllegalArgumentException("Stream URL shouldn't be null");
        this.bitrate = bitrate;
        this.streamURl = streamURl;
    }

    @Override
    public String getStreamURL() {
        return streamURl;
    }

    public Bitrate getBitrate() {
        return bitrate;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof RadioStream))
            return false;
        if (obj == this)
            return true;

        RadioStream stream = (RadioStream) obj;
        return stream.getBitrate() == this.getBitrate() && stream.getStreamURL().equals(this.getStreamURL());
    }

    @Override
    public int hashCode() {
        //TODO написать реалиацию
        int result = 17;
        result = 31 * result + streamURl.hashCode();
        result = 31 * result + bitrate.hashCode();
        return result;
    }
}
