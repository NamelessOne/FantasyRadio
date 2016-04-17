package ru.sigil.fantasyradio.schedule;

import java.util.ArrayList;
import java.util.List;

class ScheduleEntityesCollection {
    private static List<ScheduleEntity> entityes = new ArrayList<>();

    public static List<ScheduleEntity> getEntityes() {
        return entityes;
    }

    public static void setEntityes(List<ScheduleEntity> entityes) {
        ScheduleEntityesCollection.entityes = entityes;
    }

}
