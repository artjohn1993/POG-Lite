package com.generator.poglite.model

object URLData {

    data class Details (
        var url : String?,
        var id : String,
        var days: String,
        var pages: String ,
        var pauseFrom: String,
        var pauseTo: String
    )
}