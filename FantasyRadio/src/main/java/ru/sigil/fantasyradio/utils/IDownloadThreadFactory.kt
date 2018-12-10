package ru.sigil.fantasyradio.utils

import android.os.Handler
import ru.sigil.fantasyradio.saved.MP3Entity

/**
 * Created by namelessone
 * on 08.12.18.
 */
interface IDownloadThreadFactory {
    fun createDownloadThread(fileUrl: String?, fileDir: String, fileName: String, h: Handler,
                             entity: MP3Entity, errorHandler: Handler): Thread
}