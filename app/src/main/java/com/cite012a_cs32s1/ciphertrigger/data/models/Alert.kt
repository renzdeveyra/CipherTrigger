package com.cite012a_cs32s1.ciphertrigger.data.models

import java.util.Date

/**
 * Data class representing an SOS alert
 */
data class Alert(
    val id: String,
    val timestamp: Date,
    val location: Location? = null,
    val status: AlertStatus = AlertStatus.PENDING,
    val contactsNotified: List<EmergencyContact> = emptyList()
)

/**
 * Data class representing a location
 */
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val address: String? = null
) {
    /**
     * Generate a Google Maps URL for this location
     */
    fun toGoogleMapsUrl(): String {
        return "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
    }
}

/**
 * Enum representing the status of an alert
 */
enum class AlertStatus {
    PENDING,
    SENT,
    COMPLETED,
    CANCELLED
}
