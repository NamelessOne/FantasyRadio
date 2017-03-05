import org.jsoup.Connection;
import org.junit.Test;

import ru.sigil.fantasyradio.archieve.ArchieveParser;
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by
 * namelessone on 04.03.17.
 */

public class ExampleUnitTest {
    @Test(expected = WrongLoginOrPasswordException.class)
    public void ArchieveParser_Throws_WrongLoginOrPasswordException_When_LoginOrPasswordIncorrect() throws Exception {
        Connection.Response mockedResponse = mock(Connection.Response.class);
        when(mockedResponse.body()).thenReturn("Имя пользователя и пароль не совпадают или у вас еще нет учетной записи на сайте");
        ArchieveParser parser = new ArchieveParser();
        parser.Parse(mockedResponse, null);
    }
}