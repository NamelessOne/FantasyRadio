package ru.sigil.fantasyradio.saved

import ru.sigil.bassplayerlib.ITrack

/**
 * Created by namelessone
 * on 02.12.18.
 * Пока не ITrack берётся из Java, приходится костылить геттеры и сеттеры
 * @param artist Исполнитель
 * @param title Название
 * @param directory Директория
 * @param time Время (пока не используется)
 */
data class MP3Entity(private var artist: String?, private var title: String?, private var directory: String?, private var time: String?): ITrack {
    override fun setArtist(artist: String?) {
        this.artist = artist
    }

    override fun getArtist(): String? {
        return artist
    }

    override fun setTitle(title: String?) {
        this.title = title
    }

    override fun getTitle(): String? {
        return title
    }

    override fun getTime(): String? {
        return time
    }

    override fun getDirectory(): String? {
        return directory
    }

    override fun setDirectory(directory: String?) {
        this.directory = directory
    }

    override fun setTime(time: String?) {
        this.time = time
    }
}