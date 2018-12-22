package externalServicesIntegrationTests

import ru.sigil.fantasyradio.archive.ArchiveGetter
import ru.sigil.fantasyradio.archive.ArchiveParser
import ru.sigil.fantasyradio.exceptions.WrongLoginOrPasswordException
import org.junit.Assert
import org.junit.Test


/**
 * Created by namelessone
 * on 10.12.18.
 */
class ArchiveGetterTest {
    @Test
    @Throws(Exception::class)
    fun getArchiveTest_Positive() {
        val archiveParser = ArchiveParser()
        val target = ArchiveGetter(archiveParser)
        val result = target.parseArchive("NamelessOne", "fantasyradio364")
        Assert.assertNotNull(result)
        Assert.assertFalse(result.isEmpty())
        for ((URL, Name, Time) in result) {
            Assert.assertFalse(URL!!.isEmpty())
            Assert.assertFalse(Time!!.isEmpty())
            Assert.assertFalse(Name!!.isEmpty())
        }
    }

    @Test(expected = WrongLoginOrPasswordException::class)
    @Throws(Exception::class)
    fun getArchiveTest_Negative() {
        val archiveParser = ArchiveParser()
        val target = ArchiveGetter(archiveParser)
        target.parseArchive("NamelessOne", "Random")
    }
}