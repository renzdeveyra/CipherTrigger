package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.LocationRepository
import com.cite012a_cs32s1.ciphertrigger.data.repositories.PreferencesRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Setup screen
 */
class SetupViewModel(application: Application) : AndroidViewModel(application) {

    private val preferencesRepository = AppModule.providePreferencesRepository(application)
    private val contactRepository = AppModule.provideContactRepository(application)
    private val locationRepository = AppModule.provideLocationRepository(application)

    private val _setupState = MutableStateFlow(SetupState())
    val setupState: StateFlow<SetupState> = _setupState.asStateFlow()

    init {
        // Initialize by loading saved preferences
        viewModelScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            _setupState.update {
                it.copy(
                    voiceTriggerEnabled = preferences.voiceTriggerEnabled,
                    voiceTriggerPhrase = preferences.voiceTriggerPhrase
                )
            }
        }
    }

    /**
     * Check required permissions
     */
    fun checkPermissions() {
        _setupState.update {
            it.copy(
                hasContactsPermission = contactRepository.hasContactsPermission(),
                hasLocationPermission = locationRepository.hasLocationPermission()
            )
        }
    }

    /**
     * Load device contacts
     */
    fun loadDeviceContacts() {
        viewModelScope.launch {
            if (!contactRepository.hasContactsPermission()) {
                return@launch
            }

            val contacts = contactRepository.getDeviceContacts()
            _setupState.update { it.copy(deviceContacts = contacts) }
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

            // Update the selected contacts in the state
            _setupState.update {
                val updatedContacts = it.selectedContacts + emergencyContact
                it.copy(selectedContacts = updatedContacts)
            }
        }
    }

    /**
     * Remove emergency contact
     */
    fun removeEmergencyContact(contactId: String) {
        viewModelScope.launch {
            preferencesRepository.removeEmergencyContact(contactId)

            // Update the selected contacts in the state
            _setupState.update {
                val updatedContacts = it.selectedContacts.filter { contact -> contact.id != contactId }
                it.copy(selectedContacts = updatedContacts)
            }
        }
    }

    /**
     * Update voice trigger settings
     */
    fun updateVoiceTriggerSettings(enabled: Boolean, phrase: String) {
        viewModelScope.launch {
            preferencesRepository.updateVoiceTriggerSettings(enabled, phrase)

            _setupState.update {
                it.copy(
                    voiceTriggerEnabled = enabled,
                    voiceTriggerPhrase = phrase
                )
            }
        }
    }

    /**
     * Complete setup
     */
    fun completeSetup() {
        viewModelScope.launch {
            preferencesRepository.updateSetupCompleted(true)
        }
    }
}

/**
 * State for the Setup screen
 */
data class SetupState(
    val hasContactsPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val deviceContacts: List<ContactRepository.DeviceContact> = emptyList(),
    val selectedContacts: List<EmergencyContact> = emptyList(),
    val voiceTriggerEnabled: Boolean = false,
    val voiceTriggerPhrase: String = "help me"
)
