package com.generator.poglite.model

object DefaultSettings {
    data class Setting(
        var seconds: String,
        var range: String,
        var pages: String,
        var url: Array<String>,
        var time: Array<Time>,
        var factor: String
    )

    data class Time(
        var startHour : String,
        var startMin : String,
        var endHour : String,
        var endMin : String,
        var pages: String
    )
}