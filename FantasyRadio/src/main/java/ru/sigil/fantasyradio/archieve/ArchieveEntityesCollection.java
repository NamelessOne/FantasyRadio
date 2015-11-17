package ru.sigil.fantasyradio.archieve;

import java.util.ArrayList;
import java.util.List;

abstract class ArchieveEntityesCollection {
    private static List<ArchieveEntity> entityes = new ArrayList<>();

    public static List<ArchieveEntity> getEntityes() {
        return entityes;
    }
}
