package ru.sigil.fantasyradio.saved

import ru.sigil.bassplayerlib.ITrack

/**
 * Created by namelessone
 * on 02.12.18.
 * @param artist Исполнитель
 * @param title Название
 * @param directory Директория
 * @param time Время (пока не используется)
 */
data class MP3Entity(override val artist: String?, override val title: String?,
                     override var directory: String, override val time: String?): ITrack