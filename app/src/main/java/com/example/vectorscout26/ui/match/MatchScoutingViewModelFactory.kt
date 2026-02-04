package com.example.vectorscout26.ui.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vectorscout26.data.repository.ScoutRepository

class MatchScoutingViewModelFactory(
    private val repository: ScoutRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MatchScoutingViewModel::class.java)) {
            return MatchScoutingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
