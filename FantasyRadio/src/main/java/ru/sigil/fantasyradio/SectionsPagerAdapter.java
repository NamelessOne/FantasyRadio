package ru.sigil.fantasyradio;

/**
 * Created by namelessone
 * on 13.06.17.
 */

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.sigil.fantasyradio.archieve.ArchieveFragment;
import ru.sigil.fantasyradio.saved.SavedFragment;
import ru.sigil.fantasyradio.schedule.ScheduleFragment;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    private RadioFragment radioFragment = new RadioFragment();
    private ScheduleFragment scheduleFragment = new ScheduleFragment();
    private ArchieveFragment archieveFragment = new ArchieveFragment();
    private SavedFragment savedFragment = new SavedFragment();
    private Context context;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DummySectionFragment (defined as a static inner class
        // below) with the page number as its lone argument.
        //TODO
        Fragment fragment = new Fragment();
        switch (position) {
            case 0:
                fragment = radioFragment;
                break;
            case 1:
                fragment = scheduleFragment;
                break;
            case 2:
                fragment = archieveFragment;
                break;
            case 3:
                fragment = savedFragment;
                break;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.first_tab_text);
            case 1:
                return context.getString(R.string.second_tab_text);
            case 2:
                return context.getString(R.string.third_tab_text);
            case 3:
                return context.getString(R.string.fourth_tab_text);
        }
        return null;
    }

    public void notifySavedFragment() {
        savedFragment.notifyAdapter();
    }
}