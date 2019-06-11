package ru.sigil.fantasyradio.utils

import ru.sigil.fantasyradio.saved.MP3Entity
import ru.sigil.bassplayerlib.ITrack
import ru.sigil.bassplayerlib.ITrackFactory

/**
 * Created by namelessone
 * on 07.12.18.
 */
class TrackFactory : ITrackFactory {
    override fun createTrack(author: String?, title: String?, recDirectory: String, time: String?): ITrack = MP3Entity(author, title, recDirectory, time)
}
