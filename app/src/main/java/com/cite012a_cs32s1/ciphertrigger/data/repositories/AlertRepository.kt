package com.cite012a_cs32s1.ciphertrigger.data.repositories

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import androidx.core.app.ActivityCompat
import com.cite012a_cs32s1.ciphertrigger.data.models.Alert
import com.cite012a_cs32s1.ciphertrigger.data.models.AlertStatus
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.models.Location
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Date
import java.util.UUID

/**
 * Repository for managing alerts
 */
class AlertRepository(private val context: Context) {

    private val _alerts = MutableStateFlow<List<Alert>>(emptyList())
    val alerts: StateFlow<List<Alert>> = _alerts.asStateFlow()

    private val _currentAlert = MutableStateFlow<Alert?>(null)
    val currentAlert: StateFlow<Alert?> = _currentAlert.asStateFlow()

    /**
     * Create a new alert
     */
    fun createAlert(location: Location? = null): Alert {
        val alert = Alert(
            id = UUID.randomUUID().toString(),
            timestamp = Date(),
            location = location,
            status = AlertStatus.PENDING
        )

        _alerts.update { currentAlerts ->
            currentAlerts + alert
        }

        _currentAlert.value = alert

        return alert
    }

    /**
     * Update alert status
     */
    fun updateAlertStatus(alertId: String, status: AlertStatus) {
        _alerts.update { currentAlerts ->
            currentAlerts.map { alert ->
                if (alert.id == alertId) {
                    alert.copy(status = status)
                } else {
                    alert
                }
            }
        }

        if (_currentAlert.value?.id == alertId) {
            _currentAlert.update { it?.copy(status = status) }
        }
    }

    /**
     * Send alert to emergency contacts
     * Primary method is SMS, secondary is phone call
     */
    fun sendAlert(alertId: String, contacts: List<EmergencyContact>, location: Location? = null): Boolean {
        val alert = _alerts.value.find { it.id == alertId } ?: return false

        // Update alert with contacts notified
        _alerts.update { currentAlerts ->
            currentAlerts.map { currentAlert ->
                if (currentAlert.id == alertId) {
                    currentAlert.copy(
                        status = AlertStatus.SENT,
                        contactsNotified = contacts,
                        location = location ?: currentAlert.location
                    )
                } else {
                    currentAlert
                }
            }
        }

        if (_currentAlert.value?.id == alertId) {
            _currentAlert.update {
                it?.copy(
                    status = AlertStatus.SENT,
                    contactsNotified = contacts,
                    location = location ?: it.location
                )
            }
        }

        // First, send SMS to all contacts (primary alert method)
        contacts.forEach { contact ->
            if (contact.sendSms) {
                sendSms(contact.phoneNumber, createAlertMessage(location))
            }
        }

        // Then, make calls if configured (secondary alert method)
        contacts.forEach { contact ->
            if (contact.makeCall) {
                makeCall(contact.phoneNumber)
            }
        }

        return true
    }

    /**
     * Complete an alert
     */
    fun completeAlert(alertId: String) {
        updateAlertStatus(alertId, AlertStatus.COMPLETED)
    }

    /**
     * Cancel an alert
     */
    fun cancelAlert(alertId: String) {
        updateAlertStatus(alertId, AlertStatus.CANCELLED)
    }

    /**
     * Get alert by ID
     */
    fun getAlert(alertId: String): Alert? {
        return _alerts.value.find { it.id == alertId }
    }

    /**
     * Clear current alert
     */
    fun clearCurrentAlert() {
        _currentAlert.value = null
    }

    /**
     * Send SMS to a phone number
     */
    private fun sendSms(phoneNumber: String, message: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Make a phone call
     */
    private fun makeCall(phoneNumber: String) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        try {
            val intent = Intent(Intent.ACTION_CALL)
            intent.data = Uri.parse("tel:$phoneNumber")
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Create alert message with location
     */
    private fun createAlertMessage(location: Location?): String {
        val baseMessage = "EMERGENCY ALERT: I need help!"

        return if (location != null) {
            "$baseMessage My current location: ${location.toGoogleMapsUrl()}"
        } else {
            baseMessage
        }
    }
}
