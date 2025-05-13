package com.cite012a_cs32s1.ciphertrigger.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.R.drawable
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.ui.components.SOSButton
import com.cite012a_cs32s1.ciphertrigger.ui.components.StatusIndicator
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme

/**
 * Main dashboard screen with SOS button and status indicators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToSettings: () -> Unit = {},
    onNavigateToVoiceTriggerSettings: () -> Unit = {},
    onNavigateToLocationSettings: () -> Unit = {},
    onTriggerAlert: () -> Unit = {}
) {
    val dashboardState by viewModel.dashboardState.collectAsState()

    // Check location and microphone permissions when the screen is first displayed
    LaunchedEffect(key1 = Unit) {
        viewModel.checkLocationPermission()
        viewModel.checkMicrophonePermission()

        // The ViewModel will handle periodic checking of microphone state
        // This ensures the UI is updated when the microphone becomes unavailable
        // or when the SpeechRecognizer stops listening
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.dashboard_title)) },
                actions = {
                    IconButton(onClick = { onNavigateToSettings() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status indicators
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.status_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (!dashboardState.hasMicrophonePermission) {
                        // Show permission required card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = drawable.ic_mic),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Microphone Permission Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Voice trigger requires microphone access",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        onNavigateToVoiceTriggerSettings()
                                    }
                                ) {
                                    Text("Configure")
                                }
                            }
                        }
                    } else if (!dashboardState.isMicrophoneAvailable) {
                        // Show microphone unavailable card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(id = drawable.ic_mic),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(36.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Microphone Unavailable",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Another app is using the microphone. Voice trigger cannot be enabled.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        // Refresh microphone state
                                        viewModel.checkMicrophonePermission()
                                    }
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    } else {
                        StatusIndicator(
                            iconPainter = painterResource(id = drawable.ic_mic),
                            title = stringResource(R.string.voice_trigger_status),
                            isActive = dashboardState.voiceTriggerEnabled,
                            onToggle = { enabled ->
                                viewModel.updateVoiceTriggerStatus(enabled)
                            },
                            onNavigateToSettings = {
                                onNavigateToVoiceTriggerSettings()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    StatusIndicator(
                        icon = Icons.Default.LocationOn,
                        title = stringResource(R.string.location_services_status),
                        isActive = dashboardState.locationServicesEnabled,
                        onToggle = { enabled ->
                            viewModel.updateLocationServicesStatus(enabled)
                        },
                        onNavigateToSettings = {
                            onNavigateToLocationSettings()
                        }
                    )
                }
            }

            // SOS Button
            SOSButton(
                onClick = { onTriggerAlert() }
            )

            // Emergency contacts quick access
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.emergency_contacts_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (dashboardState.emergencyContacts.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_contacts_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(120.dp)
                        ) {
                            items(dashboardState.emergencyContacts) { contact ->
                                EmergencyContactItem(contact = contact)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Emergency contact item for the dashboard
 */
@Composable
fun EmergencyContactItem(contact: EmergencyContact) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (contact.photoUri != null) {
            AsyncImage(
                model = contact.photoUri,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .padding(end = 8.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = contact.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = contact.phoneNumber,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    CipherTriggerTheme {
        // Create a preview with mock data
        val previewContacts = listOf(
            EmergencyContact(
                id = "1",
                name = "John Doe",
                phoneNumber = "+1 (555) 123-4567",
                priority = 1,
                sendSms = true,
                makeCall = false
            ),
            EmergencyContact(
                id = "2",
                name = "Jane Smith",
                phoneNumber = "+1 (555) 987-6543",
                priority = 2,
                sendSms = true,
                makeCall = true
            )
        )

        // For preview purposes, we're not using the actual ViewModel
        Column {
            // Status Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StatusIndicator(
                        iconPainter = painterResource(id = drawable.ic_mic),
                        title = "Voice Trigger",
                        isActive = true,
                        onToggle = {},
                        onNavigateToSettings = {}
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    StatusIndicator(
                        icon = Icons.Default.LocationOn,
                        title = "Location Services",
                        isActive = true,
                        onToggle = {},
                        onNavigateToSettings = {}
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // SOS Button
            SOSButton()

            Spacer(modifier = Modifier.height(16.dp))

            // Contacts Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Emergency Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    previewContacts.forEach { contact ->
                        EmergencyContactItem(contact = contact)
                    }
                }
            }
        }
    }
}
