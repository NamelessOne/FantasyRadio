package ru.sigil.fantasyradio.schedule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ScheduleEntityesCollection {

    private List<ScheduleEntity> entityes = new ArrayList<>();

    @Inject
    public ScheduleEntityesCollection()
    {

    }

    public List<ScheduleEntity> getEntityes() {
        return entityes;
    }
}
