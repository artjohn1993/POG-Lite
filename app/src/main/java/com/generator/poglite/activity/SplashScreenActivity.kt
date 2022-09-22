package com.generator.poglite.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.generator.poglite.R
import com.generator.poglite.defaultSettings
import com.generator.poglite.model.DefaultSettings
import org.jetbrains.anko.startActivity

class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        createDefaultSettings()
        Handler().postDelayed({
            startActivity<MainActivity>()
            finish()
        }, 2000)

    }

    fun createDefaultSettings() {
        var url = arrayOf(
            "pageone.ph",
            "woman.ph",
            "mnlmag.com",
            "thephilippinepost.com",
            "theluzondaily.com",
            "thevisayasjournal.com",
            "themindanaolife.com",
            "politico.ph",
            "modusvivendi.net"
        )
        var time = arrayOf(
            DefaultSettings.Time("1", "0", "3", "0", "20"),
            DefaultSettings.Time("3", "1", "6", "0", "20"),
            DefaultSettings.Time("6", "1", "10", "0", "30"),
            DefaultSettings.Time("10", "1", "14", "0", "20"),
            DefaultSettings.Time("14", "1", "23", "59", "30")
        )

        var default = DefaultSettings.Setting(
            "90",
            "30",
            "30",
            url,
            time,
            "25"
        )

        defaultSettings = default
    }
}