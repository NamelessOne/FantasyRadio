import android.app.Instrumentation;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.view.KeyEvent;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import ru.sigil.fantasyradio.Gratitude;
import ru.sigil.fantasyradio.TabHoster;


@RunWith(AndroidJUnit4.class)
public class ActivityFunctionalTest extends
        ActivityInstrumentationTestCase2<TabHoster> {

    public ActivityFunctionalTest() {
        super(TabHoster.class);
    }

    //TODO придумать тест

    @Test
    @Ignore
    public void Gratitude_Should_Be_Shown_When_App_Close_First_Time() throws Exception {
        try {
            injectInstrumentation(InstrumentationRegistry.getInstrumentation());
            TabHoster activity = getActivity();
            final SharedPreferences sharedPrefs = Mockito.mock(SharedPreferences.class);
            final Context context = Mockito.mock(Context.class);
            Mockito.when(sharedPrefs.getBoolean(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(false);
            Mockito.when(context.getSharedPreferences(Mockito.anyString(), Mockito.anyInt())).thenReturn(sharedPrefs);
            Instrumentation.ActivityMonitor activityMonitor = getInstrumentation().addMonitor(Gratitude.class.getName(), null, false);
            activity.onKeyDown(KeyEvent.KEYCODE_BACK, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
            Gratitude gratitude = (Gratitude) getInstrumentation().waitForMonitorWithTimeout(activityMonitor, 5000);
            assertNotNull(gratitude);
            gratitude.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}