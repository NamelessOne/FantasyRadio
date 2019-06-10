package ru.sigil.fantasyradio

import androidx.fragment.app.Fragment
import android.widget.ListView

/**
 * Created by namelessone
 * on 08.12.18.
 */
abstract class AbstractListFragment: Fragment() {
    protected var lv: ListView? = null
}