package externalServicesIntegrationTests;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import ru.sigil.fantasyradio.schedule.ScheduleEntity;
import ru.sigil.fantasyradio.schedule.ScheduleParser;

/**
 * Created by
 * namelessone on 19.03.17.
 */

public class ScheduleGetterTest {
    @Test
    public void Get_Schedule_Positive_Test() throws Exception
    {
        ScheduleParser scheduleParser = new ScheduleParser();
        List<ScheduleEntity> result = scheduleParser.parseSchedule();
        Assert.assertNotNull(result);
        Assert.assertFalse(result.isEmpty());
        for (ScheduleEntity entity : result) {
            Assert.assertFalse(entity.getText().isEmpty());
            Assert.assertFalse(entity.getTitle().isEmpty());
            Assert.assertNotNull(entity.getEndDate());
            Assert.assertNotNull(entity.getStartDate());
        }
    }
}
