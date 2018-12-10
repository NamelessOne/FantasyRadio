package ru.sigil.fantasyradio.utils

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface IRadioStreamFactory {
    fun createDefaultStream(): RadioStream
    fun createStreamWithBitrate(bitrate: Bitrate): RadioStream
}