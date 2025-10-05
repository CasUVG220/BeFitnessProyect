package com.befitnessapp

import android.app.Application

class BeFitnessApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Graph.init(this)
    }
}
