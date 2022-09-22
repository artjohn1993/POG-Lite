package com.generator.poglite.data

import android.os.CountDownTimer

class CustomTimer(completionHandler: () -> Unit) {

    val timer = object : CountDownTimer(10 * 1000, 1000) {
        override fun onTick(millisUntilFinished: Long) {}

        override fun onFinish() {
            completionHandler.invoke()
        }
    }

    fun start() {
        timer.start()
    }

    fun cancel() {
        timer.cancel()
    }
}