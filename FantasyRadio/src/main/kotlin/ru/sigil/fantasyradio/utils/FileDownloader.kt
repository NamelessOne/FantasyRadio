package ru.sigil.fantasyradio.utils

import android.support.v4.app.NotificationCompat
import android.app.NotificationManager
import android.content.Context
import javax.inject.Inject
import android.os.Bundle
import android.os.Message
import ru.sigil.fantasyradio.R
import ru.sigil.fantasyradio.saved.MP3Entity
import java.net.URL
import java.io.*

private const val DOWNLOAD_NOTIFICATION_ID = 364
private const val BUFFER_SIZE = 4096


/**
 * Created by namelessone
 * on 05.12.18.
 */
class FileDownloader @Inject constructor(private val context: Context) : IFileDownloader {
    private val reservedChars = arrayOf("|", "\\", "?", "*", "<", "\"", ":", ">", "+", "[", "]", "/", "'", "%")

    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null

    /**
     * Скачиваем файл с сервера
     *
     * @param fileUrl  URl файла. Например http://fantasyradioru.no-ip.biz/Archive/Day/10-Radio_Fantasy_archive.mp3
     * @param fileDir  Директория для сохранения. Например /mnt/sdcard/fantasyradio/mp3/
     * @param fileName Имя файла. Например 31-Aug-201310-Radio_Fantasy_archive.mp3
     */
    @Synchronized
    override fun downloadFile(fileUrl: String, fileDir: String, fileName: String, title: String?, time: String?): MP3Entity? {
        var normalizedFileName = fileName
        for (s in reservedChars) {
            normalizedFileName = fileName.replace(s, "_")
        }
        builder = NotificationCompat.Builder(context)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setCategory(NotificationCompat.CATEGORY_PROGRESS)
                .setContentTitle(context.getString(R.string.download_started))
                .setContentText(normalizedFileName)
                .setSmallIcon(R.drawable.download)
                .setAutoCancel(false)
        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager?.notify(DOWNLOAD_NOTIFICATION_ID, builder?.build())// !!!
        val bundle = Bundle()
        bundle.putString("title", normalizedFileName)
        val titleMsg = Message()
        titleMsg.data = bundle
        try {
            val url = URL(fileUrl)
            val direct = File(fileDir)
            direct.mkdirs()
            val myFolder = File(fileDir + normalizedFileName)
            try {
                myFolder.delete()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            myFolder.createNewFile()
            val ucon = url.openConnection()
            val contentLength = ucon.contentLength
            var currentLength = 0
            var progress: Int
            val inputStream = ucon.getInputStream()
            val bis = BufferedInputStream(inputStream, BUFFER_SIZE)
            val fos = FileOutputStream(myFolder)
            val baf = ByteArray(BUFFER_SIZE)
            var length = 0
            var x = 0

            while ({length = bis.read(baf); length}() > 0) {
                currentLength += length
                fos.write(baf, 0, length)
                if (x % 1000 == 0) {
                    progress = currentLength / (contentLength / 100)
                    setNotificationProgress(progress)
                }
                x++
            }
            setNotificationProgress(100)
            fos.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        } finally {
            notificationManager?.cancel(DOWNLOAD_NOTIFICATION_ID)// !!!
        }
        return MP3Entity(fileName.substring(0, 11), title, fileDir + fileName, time)
    }

    /**
     * Устанавливаети прогресс загрузки файла из архива
     *
     * @param progress прогресс загрузки в процентах (0 - 100)
     */
    private fun setNotificationProgress(progress: Int) {
        builder?.setProgress(100, progress, false)
        notificationManager?.notify(DOWNLOAD_NOTIFICATION_ID, builder?.build())
    }
}