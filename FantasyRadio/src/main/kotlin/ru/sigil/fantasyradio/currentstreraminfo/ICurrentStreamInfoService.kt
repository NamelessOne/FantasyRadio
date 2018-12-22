package ru.sigil.fantasyradio.currentstreraminfo

/**
 * Created by namelessone
 * on 09.12.18.
 */
interface ICurrentStreamInfoService {
    fun updateInfo(callback: ICurrentStreamInfoUpdater)
}