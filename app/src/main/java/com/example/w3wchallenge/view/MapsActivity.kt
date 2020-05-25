package com.example.w3wchallenge.view

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.w3wchallenge.R
import com.example.w3wchallenge.extensions.getViewModel
import com.example.w3wchallenge.extensions.io
import com.example.w3wchallenge.extensions.main
import com.example.w3wchallenge.extensions.requestLocation
import com.example.w3wchallenge.view.SearchActivity.Companion.EXTRA_KEY_WORD
import com.example.w3wchallenge.viewmodel.MapsViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.what3words.javawrapper.response.Square
import dagger.android.support.DaggerAppCompatActivity
import kotlinx.android.synthetic.main.activity_maps.*
import timber.log.Timber
import javax.inject.Inject

class MapsActivity : DaggerAppCompatActivity(), OnMapReadyCallback {

    companion object {
        internal const val REQUEST_VIEW_SEARCH = 6001
        const val LOCATION_PERMISSION = 1001
        const val PINT_TO_RECT_ZOOM = 18.5f
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MapsViewModel by lazy {
        getViewModel(MapsViewModel::class.java, viewModelFactory)
    }

    private lateinit var mMap: GoogleMap
    private var currentPolygon: Polygon? = null
    private var currentMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        holderCurrentWord.setOnClickListener {
            startActivityForResult(Intent(this, SearchActivity::class.java), REQUEST_VIEW_SEARCH)
        }

        viewModel.mapUpdate.observe(this, Observer {
            if (it?.success == true) {
                currentWord.text = resources.getString(R.string.w3w_word, it.words)
                handleMarker(it.lat, it.lng)
                handlePolygon(it.square)
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(it.lat, it.lng), 19f
                    )
                )
            } else if (!it.error.isNullOrEmpty()) {
                Snackbar.make(holderMap, it.error!!, Snackbar.LENGTH_LONG).show()
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_VIEW_SEARCH && resultCode == Activity.RESULT_OK) {
            viewModel.convertToCoordinates(data?.getStringExtra(EXTRA_KEY_WORD))
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION -> {
                if (grantResults.count() > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    handleCurrentLocation()
                } else {
                    Snackbar.make(
                        holderMap,
                        R.string.location_permission_accuracy,
                        Snackbar.LENGTH_INDEFINITE
                    ).apply {
                        setAction(R.string.action_allow) {
                            requestPermissions(
                                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                                LOCATION_PERMISSION
                            )
                            this.dismiss()
                        }
                    }.show()
                }
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setPadding(0, resources.getDimensionPixelSize(R.dimen.main_xl), 0, 0)
        mMap.setOnCameraMoveListener {
            currentPolygon?.isVisible = mMap.cameraPosition.zoom >= PINT_TO_RECT_ZOOM
            currentMarker?.isVisible = mMap.cameraPosition.zoom < PINT_TO_RECT_ZOOM
        }
        mMap.setOnMapClickListener { latLng ->
            onMapClicked(latLng)
        }
        try {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                handleCurrentLocation(true)
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION
                )
            }
        } catch (e: SecurityException) {
            Timber.e(e)
        }
    }

    private fun onMapClicked(latLng: LatLng) {
        viewModel.convertTo3wa(latLng.latitude, latLng.longitude)
    }

    private fun handleCurrentLocation(forceRequest: Boolean = false) {
        io {
            requestLocation(forceRequest)?.let {
                viewModel.convertTo3wa(it.latitude, it.longitude)
                main {
                    mMap.isMyLocationEnabled = true
                    mMap.uiSettings.isMyLocationButtonEnabled = true
                    mMap.setOnMyLocationButtonClickListener {
                        handleCurrentLocation()
                        return@setOnMyLocationButtonClickListener true
                    }
                }
            } ?: run {
                main {
                    Snackbar.make(holderMap, R.string.location_error, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun handlePolygon(square: Square) {
        currentPolygon?.remove()
        currentPolygon = mMap.addPolygon(
            PolygonOptions()
                .add(LatLng(square.northeast.lat, square.northeast.lng))
                .add(LatLng(square.southwest.lat, square.northeast.lng))
                .add(LatLng(square.southwest.lat, square.southwest.lng))
                .add(LatLng(square.northeast.lat, square.southwest.lng))
                .strokeColor(Color.BLACK)
        )
    }

    private fun handleMarker(centerLat: Double, centerLng: Double) {
        currentMarker?.remove()
        currentMarker = mMap.addMarker(
            MarkerOptions()
                .position(LatLng(centerLat, centerLng))
                .visible(false)
        )
    }
}