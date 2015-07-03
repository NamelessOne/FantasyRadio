package ru.sigil.fantasyradio.utils;

import java.util.ArrayList;
import java.util.List;


public abstract class DownladedEntityes {
    private static List<String> downloadedEntityesURLs = new ArrayList<String>();

    public static List<String> getDownloadedEntityes() {
        return downloadedEntityesURLs;
    }

    public static void setDownloadedEntityes(List<String> downloadedEntityes) {
        downloadedEntityesURLs = downloadedEntityes;
    }
}
