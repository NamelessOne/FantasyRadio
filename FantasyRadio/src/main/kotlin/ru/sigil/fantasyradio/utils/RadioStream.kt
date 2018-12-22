package ru.sigil.fantasyradio.utils

import ru.sigil.bassplayerlib.IRadioStream
import ru.sigil.bassplayerlib.StreamFormat

/**
 * Created by namelessone
 * on 06.12.18.
 */
data class RadioStream(val bitrate: Bitrate, override val streamURL: String,
                       override val streamFormat: StreamFormat) : IRadioStream