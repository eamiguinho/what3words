package com.example.w3wchallenge.dagger.module

import com.example.w3wchallenge.view.MapsActivity
import com.example.w3wchallenge.view.SearchActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    internal abstract fun mapActivity(): MapsActivity

    @ContributesAndroidInjector
    internal abstract fun searchActivity(): SearchActivity
}

