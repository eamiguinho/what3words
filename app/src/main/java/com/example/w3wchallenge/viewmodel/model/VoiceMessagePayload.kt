package com.example.w3wchallenge.viewmodel.model

import com.what3words.javawrapper.response.Suggestion

class SuggestionsPayload : BaseVoiceMessagePayload() {
    var suggestions: List<Suggestion> = emptyList()
}

open class BaseVoiceMessagePayload {
    companion object {
        const val RecognitionStarted = "RecognitionStarted"
        const val Suggestions = "Suggestions"
    }
    var message: String? = null
    var code: String? = null
    var id: String? = null
}