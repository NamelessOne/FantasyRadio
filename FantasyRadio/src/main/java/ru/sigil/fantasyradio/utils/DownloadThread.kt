package ru.sigil.fantasyradio.utils

import android.os.Handler
import ru.sigil.fantasyradio.saved.MP3Entity
import android.os.Bundle
import android.os.Message

/**
 * Created by namelessone
 * on 08.12.18.
 */
class DownloadThread(private val fileDownloader: IFileDownloader, private val fileUrl: String?,
                     private val fileDir: String, private var fileName: String,
                     private val finishedHandler: Handler, private val mp3Entity: MP3Entity,
                     private val errorHandler: Handler) : Thread() {
    private val reservedChars = arrayOf("|", "\\", "?", "*", "<", "\"", ":", ">", "+", "[", "]", "/", "'", "%")

    override fun run() {
        for (s in reservedChars) {
            fileName = fileName.replace(s, "_")
        }
        if (fileDownloader.downloadFile(fileUrl ?: "", fileDir, fileName)) {
            mp3Entity.directory = fileDir + fileName
            val msg = Message()
            val b = Bundle()
            b.putString("artist", fileName.substring(0, 11))
            b.putString("title", mp3Entity.title)
            b.putString("directory", mp3Entity.directory)
            b.putString("time", mp3Entity.time)
            // --------------------------------
            b.putString("URL", fileUrl)
            // --------------------------------
            msg.data = b
            finishedHandler.sendMessage(msg)
        } else {
            errorHandler.sendEmptyMessage(0)
        }
    }
}