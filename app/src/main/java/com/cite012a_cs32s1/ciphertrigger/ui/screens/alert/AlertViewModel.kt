package com.cite012a_cs32s1.ciphertrigger.ui.screens.alert

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.Alert
import com.cite012a_cs32s1.ciphertrigger.data.models.AlertStatus
import com.cite012a_cs32s1.ciphertrigger.data.repositories.AlertRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Alert screen
 */
class AlertViewModel(application: Application) : AndroidViewModel(application) {

    private val alertRepository = AppModule.provideAlertRepository(application)
    private val locationRepository = AppModule.provideLocationRepository(application)
    private val preferencesRepository = AppModule.providePreferencesRepository(application)

    private val _alertState = MutableStateFlow(AlertScreenState())
    val alertState: StateFlow<AlertScreenState> = _alertState.asStateFlow()

    /**
     * Initialize the alert
     */
    fun initializeAlert() {
        viewModelScope.launch {
            // Get countdown seconds from preferences
            val preferences = preferencesRepository.userPreferencesFlow.value
            _alertState.update { it.copy(countdownSeconds = preferences.alertCountdownSeconds) }

            // Get current location if enabled
            if (preferences.locationSharingEnabled && locationRepository.hasLocationPermission()) {
                val location = locationRepository.getCurrentLocation()

                // Create alert with location
                val alert = alertRepository.createAlert(location)

                _alertState.update {
                    it.copy(
                        alertId = alert.id,
                        location = location,
                        emergencyContacts = preferences.emergencyContacts
                    )
                }
            } else {
                // Create alert without location
                val alert = alertRepository.createAlert()

                _alertState.update {
                    it.copy(
                        alertId = alert.id,
                        emergencyContacts = preferences.emergencyContacts
                    )
                }
            }
        }
    }

    /**
     * Decrement countdown
     */
    fun decrementCountdown() {
        _alertState.update {
            val newCountdown = it.countdownSeconds - 1

            // If countdown reaches 0, send the alert
            if (newCountdown <= 0 && !it.alertSent) {
                sendAlert()
            }

            it.copy(countdownSeconds = newCountdown)
        }
    }

    /**
     * Send alert to emergency contacts
     */
    fun sendAlert() {
        val currentState = _alertState.value

        if (currentState.alertSent || currentState.alertId == null) {
            return
        }

        viewModelScope.launch {
            val success = alertRepository.sendAlert(
                alertId = currentState.alertId,
                contacts = currentState.emergencyContacts,
                location = currentState.location
            )

            if (success) {
                _alertState.update { it.copy(alertSent = true) }
            }
        }
    }

    /**
     * Complete the alert
     */
    fun completeAlert(): String? {
        val alertId = _alertState.value.alertId ?: return null

        alertRepository.completeAlert(alertId)
        return alertId
    }

    /**
     * Cancel the alert
     */
    fun cancelAlert() {
        val alertId = _alertState.value.alertId ?: return

        alertRepository.cancelAlert(alertId)
        alertRepository.clearCurrentAlert()
    }
}

/**
 * State for the Alert screen
 */
data class AlertScreenState(
    val alertId: String? = null,
    val countdownSeconds: Int = 5,
    val alertSent: Boolean = false,
    val location: com.cite012a_cs32s1.ciphertrigger.data.models.Location? = null,
    val emergencyContacts: List<com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact> = emptyList()
)
