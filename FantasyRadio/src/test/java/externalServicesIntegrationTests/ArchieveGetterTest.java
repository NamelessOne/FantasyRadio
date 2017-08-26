package externalServicesIntegrationTests;

import junit.framework.Assert;

import org.junit.Test;

import java.util.List;

import ru.sigil.fantasyradio.archieve.ArchieveEntity;
import ru.sigil.fantasyradio.archieve.ArchieveGetter;
import ru.sigil.fantasyradio.archieve.ArchieveParser;
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException;

/**
 * Created by namelessone
 * on 19.03.17.
 */

public class ArchieveGetterTest {
    @Test
    public void GetArchieveTest_Positive() throws Exception
    {
        ArchieveParser archieveParser = new ArchieveParser();
        ArchieveGetter target = new ArchieveGetter(archieveParser);
        List<ArchieveEntity> result = target.ParseArchieve("NamelessOne1", "fantasyradio364");
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        for (ArchieveEntity entity : result) {
            Assert.assertFalse(entity.getURL().isEmpty());
            Assert.assertFalse(entity.getTime().isEmpty());
            Assert.assertFalse(entity.getName().isEmpty());
        }
    }

    @Test(expected = WrongLoginOrPasswordException.class)
    public void GetArchieveTest_Negative() throws Exception
    {
        ArchieveParser archieveParser = new ArchieveParser();
        ArchieveGetter target = new ArchieveGetter(archieveParser);
        target.ParseArchieve("NamelessOne", "Random");
    }
}
