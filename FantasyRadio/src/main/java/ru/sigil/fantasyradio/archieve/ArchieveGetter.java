package ru.sigil.fantasyradio.archieve;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;

public class ArchieveGetter {
    private ArchieveParser archieveParser;
    private static final Object lock = new Object();

    @Inject
    public ArchieveGetter(ArchieveParser archieveParser) {
        this.archieveParser = archieveParser;
    }

    /**
     * Парсим аудиоархив на fantasyradio.ru
     *
     * @param login    Логин пользователя на сайте
     * @param password Пароль
     * @throws WrongLoginOrPasswordException Неправильная пара логин/пароль
     */
    public void ParseArchieve(String login, String password) throws WrongLoginOrPasswordException {
        synchronized (lock) {
            try {
                Connection.Response res = Jsoup
                        .connect("http://fantasyradio.ru/")
                        .timeout(45 * 1000).followRedirects(true).userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                        .ignoreContentType(true).execute();
                Document doc = res.parse();
                Map<String, String> cookies = res.cookies();
                Elements forms = doc.getElementsByClass("form-inline");
                Elements hiddens = forms.get(0).getElementsByAttributeValue("type", "hidden");
                Map<String, String> parameters = new HashMap<>();
                parameters.put("return", hiddens.get(2).attr("value"));
                parameters.put(hiddens.get(3).attr("name"), "1");
                parameters.put("username", login);
                parameters.put("password", password);
                parameters.put("task", "user.login");
                parameters.put("option", "com_users");
                res = Jsoup
                        .connect("http://fantasyradio.ru/index.php/vojti-na-sajt?task=user.login").userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0")
                        .timeout(45 * 1000).followRedirects(true).cookies(cookies)
                        .data(parameters)
                        .method(Method.POST)
                        .ignoreContentType(true).execute();
                cookies.putAll(res.cookies());
                archieveParser.Parse(res, cookies);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}