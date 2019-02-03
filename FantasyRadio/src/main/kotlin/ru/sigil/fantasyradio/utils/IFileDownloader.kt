package ru.sigil.fantasyradio.utils

import ru.sigil.fantasyradio.saved.MP3Entity

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface IFileDownloader {
    fun downloadFile(fileUrl: String, fileDir: String, fileName: String, title: String?, time: String?): MP3Entity?
}