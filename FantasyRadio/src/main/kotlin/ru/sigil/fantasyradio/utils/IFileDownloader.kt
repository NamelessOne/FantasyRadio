package ru.sigil.fantasyradio.utils

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface IFileDownloader {
    fun downloadFile(fileUrl: String, fileDir: String, fileName: String): Boolean
}