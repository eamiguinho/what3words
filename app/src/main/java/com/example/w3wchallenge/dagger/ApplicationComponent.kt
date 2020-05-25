package com.example.w3wchallenge.dagger

import android.content.Context
import com.example.w3wchallenge.What3WordsApplication
import com.example.w3wchallenge.dagger.module.*
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ActivityModule::class, ViewModelModule::class, ApiModule::class])
interface ApplicationComponent : AndroidInjector<What3WordsApplication> {
    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): ApplicationComponent
    }
}