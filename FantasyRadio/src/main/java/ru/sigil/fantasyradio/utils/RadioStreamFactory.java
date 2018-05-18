package ru.sigil.fantasyradio.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.sigil.bassplayerlib.StreamFormat;
import ru.sigil.fantasyradio.R;

/**
 * Created by
 * namelessone on 11.03.17.
 */

public class RadioStreamFactory {
    private final Map<Bitrate, String> bitratesMap;

    @Inject
    public RadioStreamFactory(Context context) {
        bitratesMap = new HashMap<>();
        bitratesMap.put(Bitrate.aac_16, context.getString(R.string.stream_url_AAC16));
        bitratesMap.put(Bitrate.mp3_32, context.getString(R.string.stream_url_MP332));
        bitratesMap.put(Bitrate.mp3_96, context.getString(R.string.stream_url_MP396));
        bitratesMap.put(Bitrate.aac_112, context.getString(R.string.stream_url_AAC112));
    }

    public RadioStream createDefaultStream() {
        return createStreamWithBitrate(Bitrate.aac_16);
    }

    public RadioStream createStreamWithBitrate(Bitrate bitrate) {
        StreamFormat streamFormat;
        switch (bitrate)
        {
            case aac_16:
            case aac_112:
                streamFormat = StreamFormat.aac;
                break;
            case mp3_96:
            case mp3_32:
            default:
                streamFormat = StreamFormat.mp3;
                break;
        }
        return new RadioStream(bitrate, bitratesMap.get(bitrate), streamFormat);
    }
}
