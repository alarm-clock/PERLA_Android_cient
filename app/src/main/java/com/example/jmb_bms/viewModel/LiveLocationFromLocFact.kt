package com.example.jmb_bms.viewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.jmb_bms.model.LocationRepo

class LiveLocationFromLocFact(private val locationRepo: LocationRepo, val ctx: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LiveLocationFromLoc::class.java)) {
            return LiveLocationFromLoc(locationRepo, ctx) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}