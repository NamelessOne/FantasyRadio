package ru.sigil.fantasyradio

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.TextView
import android.os.Bundle
import android.app.Activity
import android.net.Uri
import android.view.View


/**
 * Created by namelessone
 * on 08.12.18.
 */
class Gratitude : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grtitude)
        val tv = findViewById<TextView>(R.id.thx_content)
        tv.setOnClickListener { _ -> stop() }
    }

    private fun stop() {
        finish()
    }

    fun rateButtonClick(view: View) {
        val uri = Uri.parse("market://details?id=" + this.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            this.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

    }

    fun noThanksButtonClick(view: View) {
        stop()
    }
}