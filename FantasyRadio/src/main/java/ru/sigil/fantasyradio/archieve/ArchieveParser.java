package ru.sigil.fantasyradio.archieve;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;

/**
 * Created by
 * namelessone on 05.03.17.
 */

public class ArchieveParser {
    @Inject
    public ArchieveParser() {

    }

    public List<ArchieveEntity> Parse(Connection.Response res, Map<String, String> cookies) throws IOException, WrongLoginOrPasswordException {
        List<ArchieveEntity> archieveEntityes = new ArrayList<>();
        if (!res.body().contains("Имя пользователя и пароль не совпадают или у вас еще нет учетной записи на сайте")) { //Проверка на на правильность логина/пароля
            Document doc = Jsoup.parse(Jsoup
                    .connect("http://fantasyradio.ru/index.php/component/content/article/2-uncategorised/14-stranitsa-2").cookies(cookies).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                    .timeout(45 * 1000).followRedirects(true)
                    .ignoreContentType(true).get().toString());
            Elements mp3Elems = doc.getElementsByAttributeValue("name", "FlashVars");
            Elements tableElems = doc.getElementsByTag("table");
            Elements trElems = new Elements();
            for (Element element : tableElems) {
                if (element.attributes().get("style")
                        .equalsIgnoreCase("width: 687px; margin-left: auto; margin-right: auto;")) {
                    trElems = element.getElementsByTag("tr");
                }
            }
            for (int i = 0; i < mp3Elems.size(); i++) {
                ArchieveEntity ae = new ArchieveEntity();
                int x = mp3Elems.get(i).attr("value").lastIndexOf('=');
                ae.setURL(mp3Elems.get(i).attr("value").substring(x + 1));
                ae.setTime(trElems.get(i).getElementsByTag("td").get(0).text());
                ae.setName(trElems.get(i).getElementsByTag("td").get(1).text());
                archieveEntityes.add(ae);
            }
        } else {//TODO Залогиниться не удалось, TT. Кидаем Exception=)
            throw new WrongLoginOrPasswordException();
        }
        return archieveEntityes;
    }
}
