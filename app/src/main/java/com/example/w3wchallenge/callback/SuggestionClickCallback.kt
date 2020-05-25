package com.example.w3wchallenge.callback

import com.what3words.javawrapper.response.Suggestion

interface SuggestionClickCallback {
    fun onClick(suggestion: Suggestion)
}
