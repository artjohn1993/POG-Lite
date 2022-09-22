package com.generator.poglite.model

object LoadingUrl {
    data class Data(
        var url: String,
        var totalWordpress: String,
        var loadedWordpress: String
    )
}