package ru.sigil.fantasyradio.utils

import ru.sigil.bassplayerlib.PlayState

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface IFantasyRadioNotificationManager {
    fun updateNotification(currentTitle: String, currentArtist: String, currentState: PlayState)
    fun createNotification(currentTitle: String, currentArtist: String, currentState: PlayState)
    fun cancel()
}