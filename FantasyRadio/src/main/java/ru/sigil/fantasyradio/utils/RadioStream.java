package ru.sigil.fantasyradio.utils;

import ru.sigil.bassplayerlib.IRadioStream;
import ru.sigil.bassplayerlib.StreamFormat;

/**
 * Created by
 * namelessone on 11.03.17.
 */

public class RadioStream implements IRadioStream {

    private Bitrate bitrate;
    private String streamURl = "";
    private StreamFormat streamFormat;

    public RadioStream(Bitrate bitrate, String streamURl, StreamFormat streamFormat) {
        if(streamURl==null)
            throw new IllegalArgumentException("Stream URL shouldn't be null");
        this.bitrate = bitrate;
        this.streamURl = streamURl;
        this.streamFormat = streamFormat;
    }

    @Override
    public String getStreamURL() {
        return streamURl;
    }

    @Override
    public StreamFormat getStreamFormat() {
        return streamFormat;
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
