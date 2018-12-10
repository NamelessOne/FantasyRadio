package ru.sigil.fantasyradio.archive

/**
 * Created by namelessone
 * on 25.11.18.
 */
data class ArchiveEntity(var URL: String?, var Name: String?, var Time: String?) {
    fun getFileName(): String {
        val x = URL!!.lastIndexOf('/')
        return URL!!.substring(x + 1)
    }
}