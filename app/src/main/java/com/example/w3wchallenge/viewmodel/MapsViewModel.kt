package com.example.w3wchallenge.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.w3wchallenge.extensions.io
import com.example.w3wchallenge.extensions.main
import com.example.w3wchallenge.viewmodel.model.MapUpdate
import com.what3words.androidwrapper.What3WordsV3
import com.what3words.javawrapper.request.Coordinates
import javax.inject.Inject

class MapsViewModel @Inject constructor(private val what3WordsV3: What3WordsV3) : ViewModel() {

    val mapUpdate = MutableLiveData<MapUpdate>()

    fun convertTo3wa(lat: Double, lng: Double) {
        io {
            val res = what3WordsV3.convertTo3wa(Coordinates(lat, lng)).execute()
            main {
                mapUpdate.value = MapUpdate().apply {
                    this.success = res.isSuccessful
                    if (res.isSuccessful) {
                        this.words = res.words
                        this.square = res.square
                        this.lat = res.coordinates.lat
                        this.lng = res.coordinates.lng
                    } else {
                        this.error = res.error.message
                    }
                }
            }
        }
    }

    fun convertToCoordinates(stringExtra: String?) {
        io {
            val res = what3WordsV3.convertToCoordinates(stringExtra).execute()
            main {
                mapUpdate.value = MapUpdate().apply {
                    this.success = res.isSuccessful
                    if (res.isSuccessful) {
                        this.words = res.words
                        this.square = res.square
                        this.lat = res.coordinates.lat
                        this.lng = res.coordinates.lng
                    } else {
                        this.error = res.error.message
                    }
                }
            }
        }
    }
}