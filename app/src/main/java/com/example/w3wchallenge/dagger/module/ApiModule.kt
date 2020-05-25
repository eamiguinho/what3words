package com.example.w3wchallenge.dagger.module

import android.content.Context
import com.example.w3wchallenge.BuildConfig.W3W_API_KEY
import com.what3words.androidwrapper.What3WordsV3
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class ApiModule {

    @Singleton
    @Provides
    fun what3WordsV3Api(context: Context): What3WordsV3 =
        What3WordsV3(W3W_API_KEY, context)
}
