package com.generator.poglite.events

import android.location.Location
import android.location.LocationListener
import android.os.Bundle

class MyLocationListener() : LocationListener {
    override fun onLocationChanged(location: Location) {
        println("sample onLocationChanged")
        println(location.latitude)
    }

    override fun onProviderDisabled(provider: String) {
        println(provider)
    }

    override fun onProviderEnabled(provider: String) {
        println(provider)
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        println(provider)
    }
}