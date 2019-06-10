import android.content.Context
import androidx.test.InstrumentationRegistry.getInstrumentation
import ru.sigil.fantasyradio.Gratitude
import org.mockito.Mockito
import android.content.SharedPreferences
import ru.sigil.fantasyradio.TabHoster
import androidx.test.rule.ActivityTestRule
import androidx.test.runner.AndroidJUnit4
import android.view.KeyEvent
import org.junit.Assert.assertNotNull
import org.junit.runner.RunWith

import org.junit.Ignore
import org.junit.Test

/**
 * Created by namelessone
 * on 12.12.18.
 */
@RunWith(AndroidJUnit4::class)
class ActivityFunctionalTest : ActivityTestRule<TabHoster>(TabHoster::class.java) {

    //TODO придумать тест
    @Test
    @Ignore
    @Throws(Exception::class)
    fun gratitude_Should_Be_Shown_When_App_Close_First_Time() {
        try {
            //injectInstrumentation(getInstrumentation());
            val activity = activity
            val sharedPrefs = Mockito.mock(SharedPreferences::class.java)
            val context = Mockito.mock(Context::class.java)
            Mockito.`when`(sharedPrefs.getBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false)
            Mockito.`when`(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(sharedPrefs)
            val activityMonitor = getInstrumentation().addMonitor(Gratitude::class.java.name, null, false)
            activity.onKeyDown(KeyEvent.KEYCODE_BACK, KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
            val gratitude = getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000) as Gratitude
            assertNotNull(gratitude)
            gratitude.finish()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}