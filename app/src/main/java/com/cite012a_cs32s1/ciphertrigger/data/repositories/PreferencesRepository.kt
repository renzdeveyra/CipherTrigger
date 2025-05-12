package com.cite012a_cs32s1.ciphertrigger.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.models.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Repository for managing user preferences using DataStore
 */
class PreferencesRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

        // Preferences keys
        private val IS_SETUP_COMPLETED = booleanPreferencesKey("is_setup_completed")
        private val VOICE_TRIGGER_ENABLED = booleanPreferencesKey("voice_trigger_enabled")
        private val VOICE_TRIGGER_PHRASE = stringPreferencesKey("voice_trigger_phrase")
        private val LOCATION_SHARING_ENABLED = booleanPreferencesKey("location_sharing_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val ALERT_COUNTDOWN_SECONDS = intPreferencesKey("alert_countdown_seconds")
        private val EMERGENCY_CONTACTS = stringPreferencesKey("emergency_contacts")
        private val SEND_SMS_DEFAULT = booleanPreferencesKey("send_sms_default")
        private val MAKE_CALL_DEFAULT = booleanPreferencesKey("make_call_default")
    }

    /**
     * Get user preferences as a Flow
     */
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val isSetupCompleted = preferences[IS_SETUP_COMPLETED] ?: false
        val voiceTriggerEnabled = preferences[VOICE_TRIGGER_ENABLED] ?: false
        val voiceTriggerPhrase = preferences[VOICE_TRIGGER_PHRASE] ?: "help me"
        val locationSharingEnabled = preferences[LOCATION_SHARING_ENABLED] ?: true
        val notificationsEnabled = preferences[NOTIFICATIONS_ENABLED] ?: true
        val alertCountdownSeconds = preferences[ALERT_COUNTDOWN_SECONDS] ?: 5
        val sendSmsDefault = preferences[SEND_SMS_DEFAULT] ?: true
        val makeCallDefault = preferences[MAKE_CALL_DEFAULT] ?: false

        val contactsJson = preferences[EMERGENCY_CONTACTS] ?: "[]"
        val emergencyContacts = try {
            Json.decodeFromString<List<EmergencyContact>>(contactsJson)
        } catch (e: Exception) {
            emptyList()
        }

        UserPreferences(
            isSetupCompleted = isSetupCompleted,
            voiceTriggerEnabled = voiceTriggerEnabled,
            voiceTriggerPhrase = voiceTriggerPhrase,
            locationSharingEnabled = locationSharingEnabled,
            notificationsEnabled = notificationsEnabled,
            alertCountdownSeconds = alertCountdownSeconds,
            emergencyContacts = emergencyContacts,
            sendSmsDefault = sendSmsDefault,
            makeCallDefault = makeCallDefault
        )
    }

    /**
     * Update setup completed status
     */
    suspend fun updateSetupCompleted(isCompleted: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SETUP_COMPLETED] = isCompleted
        }
    }

    /**
     * Update voice trigger settings
     */
    suspend fun updateVoiceTriggerSettings(enabled: Boolean, phrase: String) {
        context.dataStore.edit { preferences ->
            preferences[VOICE_TRIGGER_ENABLED] = enabled
            preferences[VOICE_TRIGGER_PHRASE] = phrase
        }
    }

    /**
     * Update location sharing setting
     */
    suspend fun updateLocationSharing(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOCATION_SHARING_ENABLED] = enabled
        }
    }

    /**
     * Update notifications setting
     */
    suspend fun updateNotifications(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    /**
     * Update alert countdown seconds
     */
    suspend fun updateAlertCountdown(seconds: Int) {
        context.dataStore.edit { preferences ->
            preferences[ALERT_COUNTDOWN_SECONDS] = seconds
        }
    }

    /**
     * Update emergency contacts
     */
    suspend fun updateEmergencyContacts(contacts: List<EmergencyContact>) {
        val contactsJson = Json.encodeToString(contacts)
        context.dataStore.edit { preferences ->
            preferences[EMERGENCY_CONTACTS] = contactsJson
        }
    }

    /**
     * Add an emergency contact
     */
    suspend fun addEmergencyContact(contact: EmergencyContact) {
        val currentContacts = getCurrentEmergencyContacts()
        val updatedContacts = currentContacts + contact
        updateEmergencyContacts(updatedContacts)
    }

    /**
     * Remove an emergency contact
     */
    suspend fun removeEmergencyContact(contactId: String) {
        val currentContacts = getCurrentEmergencyContacts()
        val updatedContacts = currentContacts.filter { it.id != contactId }
        updateEmergencyContacts(updatedContacts)
    }

    /**
     * Get current emergency contacts
     */
    private suspend fun getCurrentEmergencyContacts(): List<EmergencyContact> {
        val preferences = context.dataStore.data.map { it[EMERGENCY_CONTACTS] ?: "[]" }.first()
        return try {
            Json.decodeFromString<List<EmergencyContact>>(preferences)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Update send SMS default setting
     */
    suspend fun updateSendSmsDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SEND_SMS_DEFAULT] = enabled
        }
    }

    /**
     * Update make call default setting
     */
    suspend fun updateMakeCallDefault(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MAKE_CALL_DEFAULT] = enabled
        }
    }
}
