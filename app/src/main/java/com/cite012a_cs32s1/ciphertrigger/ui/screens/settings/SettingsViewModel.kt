package com.cite012a_cs32s1.ciphertrigger.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppModule.providePreferencesRepository(application)
    private val contactRepository = AppModule.provideContactRepository(application)
    private val locationRepository = AppModule.provideLocationRepository(application)

    val settingsState: StateFlow<SettingsState> = preferencesRepository.userPreferencesFlow
        .map { preferences ->
            SettingsState(
                voiceTriggerEnabled = preferences.voiceTriggerEnabled,
                voiceTriggerPhrase = preferences.voiceTriggerPhrase,
                locationSharingEnabled = preferences.locationSharingEnabled,
                notificationsEnabled = preferences.notificationsEnabled,
                alertCountdownSeconds = preferences.alertCountdownSeconds,
                emergencyContacts = preferences.emergencyContacts,
                hasContactsPermission = contactRepository.hasContactsPermission(),
                hasLocationPermission = locationRepository.hasLocationPermission()
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState()
        )

    /**
     * Update voice trigger settings
     */
    fun updateVoiceTriggerSettings(enabled: Boolean, phrase: String) {
        viewModelScope.launch {
            preferencesRepository.updateVoiceTriggerSettings(enabled, phrase)
        }
    }

    /**
     * Update location sharing setting
     */
    fun updateLocationSharing(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateLocationSharing(enabled)
        }
    }

    /**
     * Update notifications setting
     */
    fun updateNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateNotifications(enabled)
        }
    }

    /**
     * Update alert countdown seconds
     */
    fun updateAlertCountdown(seconds: Int) {
        viewModelScope.launch {
            preferencesRepository.updateAlertCountdown(seconds)
        }
    }

    /**
     * Add emergency contact
     */
    fun addEmergencyContact(
        deviceContact: ContactRepository.DeviceContact,
        phoneNumber: String,
        priority: Int = 0,
        sendSms: Boolean = true,
        makeCall: Boolean = false
    ) {
        val emergencyContact = contactRepository.convertToEmergencyContact(
            deviceContact = deviceContact,
            phoneNumber = phoneNumber,
            priority = priority,
            sendSms = sendSms,
            makeCall = makeCall
        )

        viewModelScope.launch {
            preferencesRepository.addEmergencyContact(emergencyContact)
        }
    }

    /**
     * Remove emergency contact
     */
    fun removeEmergencyContact(contactId: String) {
        viewModelScope.launch {
            preferencesRepository.removeEmergencyContact(contactId)
        }
    }

    /**
     * Load device contacts
     */
    suspend fun loadDeviceContacts(): List<ContactRepository.DeviceContact> {
        if (!contactRepository.hasContactsPermission()) {
            return emptyList()
        }

        return contactRepository.getDeviceContacts()
    }
}

/**
 * State for the Settings screen
 */
data class SettingsState(
    val voiceTriggerEnabled: Boolean = false,
    val voiceTriggerPhrase: String = "help me",
    val locationSharingEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val alertCountdownSeconds: Int = 5,
    val emergencyContacts: List<EmergencyContact> = emptyList(),
    val hasContactsPermission: Boolean = false,
    val hasLocationPermission: Boolean = false
)
