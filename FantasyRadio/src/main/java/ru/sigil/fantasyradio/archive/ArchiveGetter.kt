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
    @Throws(WrongLoginOrPasswordException::class)
    fun parseArchive(login: String, password: String): List<ArchiveEntity> {
        try {
            var res: Connection.Response = Jsoup
                    .connect("http://fantasyradio.ru/")
                    .timeout(45 * 1000).followRedirects(true).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                    .ignoreContentType(true).execute()
            val doc = res.parse()
            val cookies = res.cookies()
            val forms = doc.getElementsByClass("form-inline")
            val hiddens = forms[0].getElementsByAttributeValue("type", "hidden")
            val parameters = HashMap<String, String>()
            parameters["return"] = hiddens[2].attr("value")
            parameters[hiddens[3].attr("name")] = "1"
            parameters["username"] = login
            parameters["password"] = password
            parameters["task"] = "user.login"
            parameters["option"] = "com_users"
            res = Jsoup
                    .connect("http://fantasyradio.ru/index.php/vojti-na-sajt?task=user.login").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                    .timeout(45 * 1000).followRedirects(true).cookies(cookies)
                    .data(parameters)
                    .method(Connection.Method.POST)
                    .ignoreContentType(true).execute()
            cookies.putAll(res.cookies())
            return archiveParser.parse(res, cookies)
        } catch (e: IOException) {
            e.printStackTrace()
            return ArrayList()
        }
    }
}