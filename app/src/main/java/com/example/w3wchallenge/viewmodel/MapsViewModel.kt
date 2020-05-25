package com.example.w3wchallenge.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.w3wchallenge.extensions.ioThenMain
import com.example.w3wchallenge.viewmodel.model.MapUpdate
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.Coordinates
import javax.inject.Inject

class MapsViewModel @Inject constructor(private val what3WordsV3: What3WordsV3) : ViewModel() {

    val mapUpdate = MutableLiveData<MapUpdate>()

    fun convertTo3wa(lat: Double, lng: Double) {
        ioThenMain({
            what3WordsV3.convertTo3wa(Coordinates(lat, lng)).execute()
        }, {
            it?.let {
                mapUpdate.value = MapUpdate().apply {
                    this.success = it.isSuccessful
                    if (it.isSuccessful) {
                        this.words = it.words
                        this.square = it.square
                        this.lat = it.coordinates.lat
                        this.lng = it.coordinates.lng
                    } else {
                        this.error = it.error.message
                    }
                }
            }
        })
    }

    fun convertToCoordinates(stringExtra: String?) {
        ioThenMain({
            what3WordsV3.convertToCoordinates(stringExtra).execute()
        }, {
            it?.let {
                mapUpdate.value = MapUpdate().apply {
                    this.success = it.isSuccessful
                    if (it.isSuccessful) {
                        this.words = it.words
                        this.square = it.square
                        this.lat = it.coordinates.lat
                        this.lng = it.coordinates.lng
                    } else {
                        this.error = it.error.message
                    }
                }
            }
        })
    }
}