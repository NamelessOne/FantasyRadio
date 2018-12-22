package externalServicesIntegrationTests

import org.junit.Assert
import org.junit.Test
import ru.sigil.fantasyradio.schedule.ScheduleParser



/**
 * Created by namelessone
 * on 11.12.18.
 */
class ScheduleGetterTest {
    @Test
    @Throws(Exception::class)
    fun get_Schedule_Positive_Test() {
        val scheduleParser = ScheduleParser()
        val result = scheduleParser.parseSchedule()
        Assert.assertNotNull(result)
        Assert.assertFalse(result.isEmpty())
        for ((startDate, endDate, title, _, text) in result) {
            Assert.assertFalse(text!!.isEmpty())
            Assert.assertFalse(title!!.isEmpty())
            Assert.assertNotNull(endDate)
            Assert.assertNotNull(startDate)
        }
    }
}