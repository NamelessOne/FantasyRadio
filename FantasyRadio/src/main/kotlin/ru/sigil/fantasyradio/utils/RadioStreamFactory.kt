package ru.sigil.fantasyradio.utils

import android.content.Context
import ru.sigil.fantasyradio.R
import javax.inject.Inject
import ru.sigil.bassplayerlib.StreamFormat

/**
 * Created by namelessone
 * on 08.12.18.
 */
class RadioStreamFactory @Inject constructor(context: Context) : IRadioStreamFactory {
    private val bitRatesMap: Map<Bitrate, String>

    init {
        bitRatesMap = HashMap()
        bitRatesMap.put(Bitrate.AAC_16, context.getString(R.string.stream_url_AAC16))
        bitRatesMap.put(Bitrate.MP3_32, context.getString(R.string.stream_url_MP332))
        bitRatesMap.put(Bitrate.MP3_96, context.getString(R.string.stream_url_MP396))
        bitRatesMap.put(Bitrate.AAC_112, context.getString(R.string.stream_url_AAC112))
    }

    override fun createDefaultStream(): RadioStream {
        return createStreamWithBitrate(Bitrate.AAC_16)
    }

    override fun createStreamWithBitrate(bitrate: Bitrate): RadioStream {
        val streamFormat: StreamFormat = when (bitrate) {
            Bitrate.AAC_16, Bitrate.AAC_112 -> StreamFormat.AAC
            Bitrate.MP3_96, Bitrate.MP3_32 -> StreamFormat.MP3
        }
        return RadioStream(bitrate, bitRatesMap[bitrate]!!, streamFormat)
    }
}