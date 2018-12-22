package ru.sigil.fantasyradio.utils

import ru.sigil.bassplayerlib.ITrack
import ru.sigil.bassplayerlib.ITracksCollection
import ru.sigil.fantasyradio.saved.MP3Entity

/**
 * Created by namelessone
 * on 16.12.18.
 */
interface IFantasyRadioTracksCollection: ITracksCollection {
    fun getNext(entity: ITrack?): MP3Entity?
}