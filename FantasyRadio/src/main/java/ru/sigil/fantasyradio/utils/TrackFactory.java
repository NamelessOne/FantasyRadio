package ru.sigil.fantasyradio.utils;

import ru.sigil.fantasyradio.BackgroundService.ITrack;
import ru.sigil.fantasyradio.BackgroundService.ITrackFactory;
import ru.sigil.fantasyradio.saved.MP3Entity;

/**
 * Created by namelessone
 * on 04.01.17.
 */

public class TrackFactory implements ITrackFactory{
    @Override
    public ITrack createTrack(String author, String title, String recDirectory, String time) {
        return new MP3Entity(author, title, recDirectory, time);
    }
}
