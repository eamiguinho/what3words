package com.example.w3wchallenge.extensions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.location.LocationManagerCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

fun <T : Any> ioThenMain(work: suspend (() -> T?), callback: ((T?) -> Unit)): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        val data = CoroutineScope(Dispatchers.IO).async rt@{
            return@rt work()
        }.await()
        callback(data)
    }
}

fun io(work: suspend (() -> Unit)): Job {
    return CoroutineScope(Dispatchers.IO).launch {
        work()
    }
}

fun main(work: suspend (() -> Unit)): Job {
    return CoroutineScope(Dispatchers.Main).launch {
        work()
    }
}

suspend fun Context.requestLocation(forceRequest: Boolean = false): Location? =
    suspendCoroutine { task ->
        val manager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!LocationManagerCompat.isLocationEnabled(manager) || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            task.resume(null)
        } else {
            val service = LocationServices.getFusedLocationProviderClient(this)
            io {
                if (forceRequest) {
                    task.resume(locationRequest(manager, service))
                } else {
                    task.resume(service.lastLocation.await())
                }
            }
        }
    }

private suspend fun locationRequest(
    locationManager: LocationManager,
    service: FusedLocationProviderClient
): Location? = suspendCoroutine { task ->
    val callback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult?) {
            service.removeLocationUpdates(this)
            task.resume(p0?.lastLocation)
        }
    }

    when {
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> {
            service.requestLocationUpdates(
                LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setNumUpdates(1)
                    .setExpirationDuration(2000),
                callback,
                Looper.getMainLooper()
            )
        }
        else -> {
            task.resume(null)
        }
    }
}

internal fun <T : ViewModel> AppCompatActivity.getViewModel(
    modelClass: Class<T>,
    viewModelFactory: ViewModelProvider.Factory? = null
): T {
    return viewModelFactory?.let { ViewModelProvider(this, viewModelFactory).get(modelClass) }
        ?: ViewModelProvider(this).get(modelClass)
}