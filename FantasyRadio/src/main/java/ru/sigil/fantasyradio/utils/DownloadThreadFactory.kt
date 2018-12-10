package ru.sigil.fantasyradio.utils

import android.os.Handler
import ru.sigil.fantasyradio.saved.MP3Entity
import javax.inject.Inject

/**
 * Created by namelessone
 * on 08.12.18.
 */
class DownloadThreadFactory @Inject constructor(private val fileDownloader: IFileDownloader): IDownloadThreadFactory {
    override fun createDownloadThread(fileUrl: String?, fileDir: String, fileName: String,
                                      h: Handler, entity: MP3Entity, errorHandler: Handler): Thread {
        return DownloadThread(fileDownloader, fileUrl, fileDir, fileName, h, entity, errorHandler)
    }
}