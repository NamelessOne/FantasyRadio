package ru.sigil.fantasyradio.archive

import org.jsoup.Connection
import javax.inject.Inject

/**
 * Created by namelessone
 * on 28.11.18.
 */
class ArchiveParser @Inject constructor() {
    fun parse(res: Connection.Response): List<ArchiveEntity> {
        val archiveEntities = ArrayList<ArchiveEntity>()
        val doc = res.parse()
        val playlist = doc.getElementsByClass("ap-playlist")[0]

        for (elem in playlist.children()) {
            val ae = ArchiveEntity(
                    elem.getElementsByClass("ap-source").attr("data-src"),
                    elem.getElementsByClass("ap-desc").text(),
                    elem.getElementsByClass("ap-source").text())
            archiveEntities.add(ae)
        }
        return archiveEntities
    }
}