package ru.sigil.fantasyradio.archive

import org.jsoup.Connection
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException
import java.io.IOException
import javax.inject.Inject

/**
 * Created by namelessone
 * on 28.11.18.
 */
class ArchiveParser @Inject constructor() {
    @Throws(IOException::class, WrongLoginOrPasswordException::class)
    fun parse(res: Connection.Response, cookies: Map<String, String>): List<ArchiveEntity> {
        val archiveEntities = ArrayList<ArchiveEntity>()
        if (!res.body().contains("Имя пользователя и пароль не совпадают или у вас еще нет учетной записи на сайте")) { //Проверка на на правильность логина/пароля
            val doc = Jsoup.parse(Jsoup
                    .connect("http://fantasyradio.ru/index.php/component/content/article/2-uncategorised/14-stranitsa-2").cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                    .timeout(45 * 1000).followRedirects(true)
                    .ignoreContentType(true).get().toString())
            val mp3Elements = doc.getElementsByAttributeValue("name", "FlashVars")
            val tableElements = doc.getElementsByTag("table")
            var trElements = Elements()
            for (element in tableElements) {
                if (element.attributes().get("style")
                                .equals("width: 687px; margin-left: auto; margin-right: auto;", ignoreCase = true)) {
                    trElements = element.getElementsByTag("tr")
                }
            }
            for (i in mp3Elements.indices) {
                val x = mp3Elements[i].attr("value").lastIndexOf('=')
                val ae = ArchiveEntity(
                        mp3Elements[i].attr("value").substring(x + 1),
                        trElements[i].getElementsByTag("td")[0].text(),
                        trElements[i].getElementsByTag("td")[1].text())
                archiveEntities.add(ae)
            }
        } else {//TODO Залогиниться не удалось, TT. Кидаем Exception=)
            throw WrongLoginOrPasswordException()
        }
        return archiveEntities
    }
}