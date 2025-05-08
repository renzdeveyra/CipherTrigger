package com.cite012a_cs32s1.ciphertrigger.data.models

import kotlinx.serialization.Serializable

/**
 * Data class representing an emergency contact
 */
@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val photoUri: String? = null,
    val priority: Int = 0,
    val sendSms: Boolean = true,
    val makeCall: Boolean = false
)
