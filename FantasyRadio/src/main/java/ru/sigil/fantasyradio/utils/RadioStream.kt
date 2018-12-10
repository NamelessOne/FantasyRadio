package ru.sigil.fantasyradio.utils

import ru.sigil.bassplayerlib.IRadioStream
import ru.sigil.bassplayerlib.StreamFormat

/**
 * Пока не IRadioStream берётся из Java, приходится костылить геттеры
 * Created by namelessone
 * on 06.12.18.
 */
data class RadioStream(private val bitrate: Bitrate, private val streamURl: String, private val streamFormat: StreamFormat) : IRadioStream {

    override fun getStreamFormat(): StreamFormat {
        return streamFormat
    }

    override fun getStreamURL(): String {
        return streamURl
    }

    fun getBitrate(): Bitrate {
        return bitrate
    }
}