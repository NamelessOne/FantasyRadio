package ru.sigil.fantasyradio

import android.app.Activity
import android.widget.TextView
import android.os.Bundle

/**
 * Окошко "О программе"
 * Created by namelessone
 * on 08.12.18.
 */
class About: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about)
        val tv = findViewById<TextView>(R.id.about_content)
        tv.append(getString(R.string.versiontext) + " "
                + this.packageManager.getPackageInfo(this.packageName, 0).versionName)
        tv.setOnClickListener { _ -> stop() }
    }

    private fun stop() = finish()
}