package com.cite012a_cs32s1.ciphertrigger.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.services.VoiceRecognitionManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
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

            // Update voice recognition service
            if (enabled) {
                VoiceRecognitionManager.startVoiceRecognition(getApplication())
            } else {
                VoiceRecognitionManager.stopVoiceRecognition(getApplication())
            }
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

    /**
     * Update contact's SMS setting
     */
    fun updateContactSendSms(contactId: String, sendSms: Boolean) {
        viewModelScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            val updatedContacts = preferences.emergencyContacts.map { contact ->
                if (contact.id == contactId) {
                    contact.copy(sendSms = sendSms)
                } else {
                    contact
                }
            }
            preferencesRepository.updateEmergencyContacts(updatedContacts)
        }
    }

    /**
     * Update contact's call setting
     */
    fun updateContactMakeCall(contactId: String, makeCall: Boolean) {
        viewModelScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            val updatedContacts = preferences.emergencyContacts.map { contact ->
                if (contact.id == contactId) {
                    contact.copy(makeCall = makeCall)
                } else {
                    contact
                }
            }
            preferencesRepository.updateEmergencyContacts(updatedContacts)
        }
    }

    /**
     * Move contact priority up (lower number = higher priority)
     */
    fun moveContactPriorityUp(contactId: String) {
        viewModelScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            val contacts = preferences.emergencyContacts.sortedBy { it.priority }.toMutableList()

            val index = contacts.indexOfFirst { it.id == contactId }
            if (index > 0) {
                // Swap with the contact above it
                val currentContact = contacts[index]
                val higherContact = contacts[index - 1]

                contacts[index] = currentContact.copy(priority = higherContact.priority)
                contacts[index - 1] = higherContact.copy(priority = currentContact.priority)

                preferencesRepository.updateEmergencyContacts(contacts)
            }
        }
    }

    /**
     * Move contact priority down (higher number = lower priority)
     */
    fun moveContactPriorityDown(contactId: String) {
        viewModelScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            val contacts = preferences.emergencyContacts.sortedBy { it.priority }.toMutableList()

            val index = contacts.indexOfFirst { it.id == contactId }
            if (index >= 0 && index < contacts.size - 1) {
                // Swap with the contact below it
                val currentContact = contacts[index]
                val lowerContact = contacts[index + 1]

                contacts[index] = currentContact.copy(priority = lowerContact.priority)
                contacts[index + 1] = lowerContact.copy(priority = currentContact.priority)

                preferencesRepository.updateEmergencyContacts(contacts)
            }
        }
    }

    /**
     * Update SMS default setting
     */
    fun updateSendSmsDefault(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateSendSmsDefault(enabled)
        }
    }

    /**
     * Update call default setting
     */
    fun updateMakeCallDefault(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.updateMakeCallDefault(enabled)
        }
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
    val hasLocationPermission: Boolean = false,
    val sendSmsDefault: Boolean = true,
    val makeCallDefault: Boolean = false
)
