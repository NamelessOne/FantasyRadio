package ru.sigil.fantasyradio.utils;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

import ru.sigil.bassplayerlib.Bitrate;
import ru.sigil.fantasyradio.R;

/**
 * Created by
 * namelessone on 17.10.16.
 */

public class BitratesResolver {
    //TODO сделать нормальный резолвер и инжектировать
    private final Map<Bitrate, String> bitratesMap;

    public BitratesResolver(Context context)
    {
        bitratesMap = new HashMap<>();
        bitratesMap.put(Bitrate.aac_16, context.getString(R.string.stream_url_AAC16));
        bitratesMap.put(Bitrate.mp3_32, context.getString(R.string.stream_url_MP332));
        bitratesMap.put(Bitrate.mp3_96, context.getString(R.string.stream_url_MP396));
        bitratesMap.put(Bitrate.aac_112, context.getString(R.string.stream_url_AAC112));
    }

    public String getUrl(Bitrate bitrate)
    {
        return bitratesMap.get(bitrate);
    }
}
