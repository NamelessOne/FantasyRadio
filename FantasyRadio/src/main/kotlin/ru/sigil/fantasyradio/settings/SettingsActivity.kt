package ru.sigil.fantasyradio.settings

import android.app.Activity
import android.widget.EditText
import android.os.Bundle
import android.view.View
import ru.sigil.fantasyradio.R
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.lamerman.FileDialog
import ru.sigil.fantasyradio.dagger.Bootstrap
import javax.inject.Inject

private const val REQUEST_SAVE = 0
private const val REQUEST_LOAD = 1

/**
 * Activity настроек
 * Created by namelessone
 * on 05.12.18.
 */
class SettingsActivity: Activity() {
    @Inject
    lateinit var settings: ISettings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Bootstrap.INSTANCE.getBootstrap().inject(this)
        setContentView(R.layout.settings)
        val et = findViewById<EditText>(R.id.settingsMP3SaveFolder)
        et.setText(settings.getSaveDir())
        // --------------------------------------------------------
        findViewById<View>(R.id.settingsButtonCancel).setOnClickListener { finish() }
        findViewById<View>(R.id.settingsButtonSave).setOnClickListener(
                saveClickListener)
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onStop() {
        super.onStop()
    }

    private val saveClickListener = { _: View ->
        val et = findViewById<EditText>(R.id.settingsMP3SaveFolder)
        val s = et.text.toString()
        settings.setSaveDir(s)
        finish()
    }

    fun selectDirClick(v: View) {
        val intent = Intent(baseContext, FileDialog::class.java)
        startActivityForResult(intent, REQUEST_SAVE)
    }

    @Synchronized
    public override fun onActivityResult(requestCode: Int,
                                         resultCode: Int, data: Intent) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_SAVE) {
                println("Saving...")
            } else if (requestCode == REQUEST_LOAD) {
                println("Loading...")
            }
            var filePath: String? = data.getStringExtra("RESULT_FOLDER")
            val mntSDcard = Environment.getExternalStorageDirectory()
                    .absolutePath
            Log.v("garbage", mntSDcard)
            if (filePath != null) {
                filePath = filePath.replace(mntSDcard, "")
            }
            Log.v("folder", filePath ?: "")
            val et = findViewById<EditText>(R.id.settingsMP3SaveFolder)
            et.setText(filePath)
        }
    }

}