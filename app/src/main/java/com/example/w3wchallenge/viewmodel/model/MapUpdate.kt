package com.example.w3wchallenge.viewmodel.model

import com.what3words.javawrapper.response.Square
import com.what3words.javawrapper.response.Suggestion

class MapUpdate : ResultBase() {
    var words: String = ""
    var square: Square = Square()
    var lat: Double = 0.0
    var lng: Double = 0.0
}

class SuggestionsUpdate : ResultBase() {
    var suggestions: List<Suggestion> = emptyList()
}

open class ResultBase(
    var success: Boolean = false,
    var error: String? = null
)