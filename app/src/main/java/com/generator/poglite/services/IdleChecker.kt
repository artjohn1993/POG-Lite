package com.generator.poglite.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.generator.poglite.events.IdleCheckerEvent
import org.greenrobot.eventbus.EventBus

class IdleChecker: Service() {
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("start na")
        Handler().postDelayed({
            EventBus.getDefault().post(IdleCheckerEvent())
        },60000)

        return super.onStartCommand(intent, flags, startId)
    }
}