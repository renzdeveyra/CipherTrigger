package com.cite012a_cs32s1.ciphertrigger.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.launch

/**
 * Screen for configuring voice trigger settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTriggerSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPermissionsSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Use the state directly from the Flow instead of local state variables

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Trigger Settings") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            if (!settingsState.hasMicrophonePermission) {
                // Permission not granted UI
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(72.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Microphone Permission Required",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "To use voice trigger, you need to grant microphone permission.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onNavigateToPermissionsSetup()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Setup Permissions")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Text(
                                text = "Voice Trigger",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.weight(1f))

                            Switch(
                                checked = settingsState.voiceTriggerEnabled,
                                onCheckedChange = { enabled ->
                                    viewModel.updateVoiceTriggerSettings(enabled, settingsState.voiceTriggerPhrase)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            if (enabled) "Voice trigger enabled" else "Voice trigger disabled"
                                        )
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "When enabled, the app will listen for your voice trigger phrase in the background and automatically trigger an SOS alert when detected.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Trigger Phrase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Enter a phrase that will trigger the SOS alert when spoken. Choose something you can say easily in an emergency but is unlikely to be said in normal conversation.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = settingsState.voiceTriggerPhrase,
                            onValueChange = { phrase ->
                                if (phrase.isNotBlank()) {
                                    viewModel.updateVoiceTriggerSettings(settingsState.voiceTriggerEnabled, phrase)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Trigger Phrase") },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Example phrases: \"help me\", \"emergency\", \"SOS\"",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Important Notes",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Voice recognition works best in quiet environments\n" +
                                  "• Battery usage will increase when voice trigger is enabled\n" +
                                  "• The app needs to be running in the background for voice trigger to work\n" +
                                  "• Voice recognition may not work if the device is in battery saving mode",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VoiceTriggerSettingsScreenPreview() {
    CipherTriggerTheme {
        VoiceTriggerSettingsScreen(onNavigateToPermissionsSetup = {})
    }
}
