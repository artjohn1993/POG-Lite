package com.generator.poglite

import com.generator.poglite.model.DefaultSettings

//var isRefresh  = false

var defaultSettings : DefaultSettings.Setting? = null

class GlobalFun {
    companion object {
        fun convertHour(hour : Int, minutes : Int) : String {
            var time : String = ""
            if (hour >= 12 ) {
                if (hour - 12 == 0) {
                    if (minutes < 10) {
                        time = "12 : 0${minutes} PM"
                    } else {
                        time = "12 : ${minutes} PM"
                    }
                } else {
                    if (minutes < 10) {
                        time = "${hour - 12} : 0${minutes} PM"
                    } else {
                        time = "${hour - 12} : ${minutes} PM"
                    }
                }
            } else {
                if (minutes < 10) {
                    time = "${hour} : 0${minutes} AM"
                } else {
                    time = "${hour} : ${minutes} AM"
                }
            }
            return time
        }
    }
}
