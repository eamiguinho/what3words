package com.example.w3wchallenge.dagger.module

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.w3wchallenge.viewmodel.MapsViewModel
import com.example.w3wchallenge.viewmodel.SearchViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(MapsViewModel::class)
    abstract fun bindCreateMapsViewModel(viewModel: MapsViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(SearchViewModel::class)
    abstract fun bindCreateSearchViewModel(viewModel: SearchViewModel): ViewModel

    @Binds
    abstract fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory
}