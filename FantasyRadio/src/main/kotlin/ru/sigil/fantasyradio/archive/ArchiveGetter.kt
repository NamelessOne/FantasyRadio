package ru.sigil.fantasyradio.archive

import javax.inject.Inject
import org.jsoup.Jsoup
import org.jsoup.Connection
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException
import java.io.IOException


/**
 * Created by namelessone
 * on 26.11.18.
 */

class ArchiveGetter @Inject constructor(private val archiveParser: ArchiveParser) {
    @Synchronized
    fun parseArchive(): List<ArchiveEntity> {
        return try {
            var res: Connection.Response = Jsoup
                    .connect("https://fantasyradio.ru/new/audio-player/dist/")
                    .timeout(45 * 1000).followRedirects(true).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                    .ignoreContentType(true).execute()
            archiveParser.parse(res)
        } catch (e: IOException) {
            e.printStackTrace()
            ArrayList()
        }
    }
}