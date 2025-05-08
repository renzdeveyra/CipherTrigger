package com.cite012a_cs32s1.ciphertrigger.ui.screens.alert

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.cite012a_cs32s1.ciphertrigger.data.models.Alert
import com.cite012a_cs32s1.ciphertrigger.data.repositories.AlertRepository
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Alert Summary screen
 */
class AlertSummaryViewModel(application: Application) : AndroidViewModel(application) {

    private val alertRepository = AppModule.provideAlertRepository(application)

    private val _summaryState = MutableStateFlow(AlertSummaryState())
    val summaryState: StateFlow<AlertSummaryState> = _summaryState.asStateFlow()

    /**
     * Load alert details
     */
    fun loadAlertDetails(alertId: String?) {
        if (alertId == null) {
            _summaryState.update { it.copy(error = "Invalid alert ID") }
            return
        }

        val alert = alertRepository.getAlert(alertId)

        if (alert == null) {
            _summaryState.update { it.copy(error = "Alert not found") }
            return
        }

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedTime = dateFormat.format(alert.timestamp)

        _summaryState.update {
            it.copy(
                alertId = alertId,
                time = formattedTime,
                location = alert.location?.address ?: "Location not available",
                locationUrl = alert.location?.toGoogleMapsUrl(),
                contactsNotified = alert.contactsNotified,
                error = null
            )
        }
    }
}

/**
 * State for the Alert Summary screen
 */
data class AlertSummaryState(
    val alertId: String? = null,
    val time: String = "",
    val location: String = "",
    val locationUrl: String? = null,
    val contactsNotified: List<com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact> = emptyList(),
    val error: String? = null
)
