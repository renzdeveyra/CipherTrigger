package com.cite012a_cs32s1.ciphertrigger.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.services.VoiceRecognitionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen
 */
class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppModule.providePreferencesRepository(application)
    private val locationRepository = AppModule.provideLocationRepository(application)

    private val _locationPermissionGranted = MutableStateFlow(locationRepository.hasLocationPermission())
    val locationPermissionGranted: StateFlow<Boolean> = _locationPermissionGranted

    val dashboardState = combine(
        preferencesRepository.userPreferencesFlow,
        _locationPermissionGranted
    ) { preferences, locationPermission ->
        DashboardState(
            voiceTriggerEnabled = preferences.voiceTriggerEnabled,
            locationServicesEnabled = locationPermission && preferences.locationSharingEnabled,
            emergencyContacts = preferences.emergencyContacts
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState()
    )

    /**
     * Update voice trigger status
     */
    fun updateVoiceTriggerStatus(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateVoiceTriggerSettings(
                enabled = enabled,
                phrase = preferencesRepository.userPreferencesFlow.value.voiceTriggerPhrase
            )

            // Update voice recognition service
            if (enabled) {
                VoiceRecognitionManager.startVoiceRecognition(getApplication())
            } else {
                VoiceRecognitionManager.stopVoiceRecognition(getApplication())
            }
        }
    }

    /**
     * Update location services status
     */
    fun updateLocationServicesStatus(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateLocationSharing(enabled)
        }
    }

    /**
     * Check location permission
     */
    fun checkLocationPermission() {
        _locationPermissionGranted.value = locationRepository.hasLocationPermission()
    }
}

/**
 * State for the Dashboard screen
 */
data class DashboardState(
    val voiceTriggerEnabled: Boolean = false,
    val locationServicesEnabled: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = emptyList()
)
