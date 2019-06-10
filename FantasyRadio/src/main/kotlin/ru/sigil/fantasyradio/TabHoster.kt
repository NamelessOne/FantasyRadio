package ru.sigil.fantasyradio

import android.Manifest
import androidx.fragment.app.FragmentActivity
import androidx.appcompat.app.AlertDialog
import ru.sigil.fantasyradio.settings.ISettings
import javax.inject.Inject
import ru.sigil.fantasyradio.utils.IFantasyRadioNotificationManager
import ru.sigil.fantasyradio.utils.RadioStream
import ru.sigil.bassplayerlib.IPlayer
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration
import com.nostra13.universalimageloader.core.DisplayImageOptions
import androidx.viewpager.widget.ViewPager
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import ru.sigil.fantasyradio.playerservice.PlayerBackgroundService
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.app.ActivityCompat
import android.view.*
import ru.sigil.fantasyradio.dagger.Bootstrap
import ru.sigil.bassplayerlib.PlayState
import ru.sigil.fantasyradio.settings.SettingsActivity
import android.widget.Toast
import ru.sigil.bassplayerlib.listeners.IPlayerErrorListener
import java.util.*
import java.util.concurrent.TimeUnit


private const val MY_PERMISSIONS_REQUEST = 1

/**
 * Created by namelessone
 * on 09.12.18.
 */
class TabHoster: FragmentActivity() {
    var mSectionsPagerAdapter: SectionsPagerAdapter? = null
    @Inject
    lateinit var player: IPlayer<RadioStream>
    @Inject
    lateinit var notificationManager: IFantasyRadioNotificationManager
    @Inject
    lateinit var settings: ISettings

    private fun requestPermissionWithRationale() {
        AlertDialog.Builder(this)
                .setTitle(getString(R.string.permissions_request_title))
                .setMessage(getString(R.string.permissions_request))
                .setPositiveButton(android.R.string.yes) { _, _ ->
                    // continue with delete
                    requestMyPermissions()
                }
                .setCancelable(false)
                .show()
    }

    private fun requestMyPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE),
                MY_PERMISSIONS_REQUEST)
    }


    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            this.startService(Intent(this, PlayerBackgroundService::class.java))
        } else {
            Handler().post { startService(Intent(this, PlayerBackgroundService::class.java)) } //Start
        }
        player.addPlayerErrorListener(playerErrorListener)
        setContentView(R.layout.tabs)
        //-------------------------------------------------------
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionWithRationale()
        }
        //-------------------------------------------------------
        CurrentMenuContainer.current_menu = R.menu.activity_main
        val localSettings = getPreferences(0)
        settings.moveLocalSettingsToGlobal(localSettings)
        mSectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val mViewPager = findViewById<ViewPager>(R.id.pager)
        mViewPager.adapter = mSectionsPagerAdapter
        val defaultOptions = DisplayImageOptions.Builder()
                .cacheOnDisk(true).build()
        val config = ImageLoaderConfiguration.Builder(baseContext)
                .defaultDisplayImageOptions(defaultOptions).build()
        ImageLoader.getInstance().init(config)
    }

    public override fun onPause() {
        if (player.playState === PlayState.PLAY
                || player.playState === PlayState.PLAY_FILE || player.playState === PlayState.BUFFERING) {
            notificationManager.createNotification(player.title, player.author, player.playState)
        } else {
            player.stop()
        }
        super.onPause()
    }

    public override fun onDestroy() {
        player.removePlayerErrorListener(playerErrorListener)
        super.onDestroy()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK && player.playState === PlayState.PLAY
                || player.playState === PlayState.PLAY_FILE || player.playState === PlayState.BUFFERING) {
            onPause()
            val intent = Intent()
            intent.action = Intent.ACTION_MAIN
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)
            return true
        }
        //--------------
        if (keyCode == KeyEvent.KEYCODE_BACK && !settings.getGratitude()) {
            settings.setGratitude(true)
            val i = Intent(applicationContext, Gratitude::class.java)
            startActivity(i)
            return true
        }
        //--------------
        return super.onKeyDown(keyCode, event)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(CurrentMenuContainer.current_menu, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        val inflater = menuInflater
        inflater.inflate(CurrentMenuContainer.current_menu, menu)
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.about -> {
                val i = Intent(this, About::class.java)
                try {
                    startActivity(i)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return true
            }
            R.id.settings -> {
                val j = Intent(this, SettingsActivity::class.java)
                try {
                    startActivity(j)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return true
            }
            R.id.exit -> {
                player.stop()
                //--------------
                if (!settings.getGratitude()) {
                    settings.setGratitude(true)
                    val intent = Intent(applicationContext, Gratitude::class.java)
                    startActivity(intent)
                    return true
                }
                //--------------
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    public override fun onResume() {
        try {
            notificationManager.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        super.onResume()
    }

    fun fabButtonClick(view: View) {
        openOptionsMenu()
    }

    private val playerErrorListener = object : IPlayerErrorListener {
        override fun onError(message: String, errorCode: Int, exception: Exception?) {
            val s = String.format(Locale.getDefault(), "%s\n(error code: %d)", message, errorCode)
            runOnUiThread {
                try {
                    exception?.printStackTrace()
                    val toast = Toast.makeText(applicationContext, s, Toast.LENGTH_SHORT)
                    toast.show()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}