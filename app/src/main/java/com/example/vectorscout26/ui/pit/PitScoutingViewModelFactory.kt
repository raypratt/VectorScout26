package com.example.vectorscout26.ui.pit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vectorscout26.data.repository.PitScoutRepository

class PitScoutingViewModelFactory(
    private val repository: PitScoutRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PitScoutingViewModel::class.java)) {
            return PitScoutingViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
