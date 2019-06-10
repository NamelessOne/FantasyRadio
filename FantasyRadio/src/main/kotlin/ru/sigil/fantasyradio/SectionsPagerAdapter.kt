package ru.sigil.fantasyradio

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ru.sigil.fantasyradio.saved.SavedFragment
import ru.sigil.fantasyradio.archive.ArchiveFragment
import ru.sigil.fantasyradio.schedule.ScheduleFragment



/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
class SectionsPagerAdapter(private val context: Context, fragmentManager: FragmentManager): FragmentPagerAdapter(fragmentManager) {
    private val radioFragment = RadioFragment()
    private val scheduleFragment = ScheduleFragment()
    private val archiveFragment = ArchiveFragment()
    private val savedFragment = SavedFragment()

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a DummySectionFragment (defined as a static inner class
        // below) with the page number as its lone argument.
        //TODO
        var fragment = Fragment()
        when (position) {
            0 -> fragment = radioFragment
            1 -> fragment = scheduleFragment
            2 -> fragment = archiveFragment
            3 -> fragment = savedFragment
        }
        return fragment
    }

    override fun getCount(): Int {
        return 4
    }

    override fun getPageTitle(position: Int): CharSequence? {
        when (position) {
            0 -> return context.getString(R.string.first_tab_text)
            1 -> return context.getString(R.string.second_tab_text)
            2 -> return context.getString(R.string.third_tab_text)
            3 -> return context.getString(R.string.fourth_tab_text)
        }
        return null
    }

    fun notifySavedFragment() {
        savedFragment.notifyAdapter()
    }
}