package com.cite012a_cs32s1.ciphertrigger.data.models

/**
 * Data class representing user preferences
 */
data class UserPreferences(
    val isSetupCompleted: Boolean = false,
    val voiceTriggerEnabled: Boolean = false,
    val voiceTriggerPhrase: String = "help me",
    val locationSharingEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val alertCountdownSeconds: Int = 5,
    val emergencyContacts: List<EmergencyContact> = emptyList()
)
