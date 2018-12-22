package ru.sigil.fantasyradio.currentstreraminfo

/**
 * Created by namelessone
 * on 01.12.18.
 */
interface ICurrentStreamInfoUpdater {
    fun update(about: String, imageURL: String)
}