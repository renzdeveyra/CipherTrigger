package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionGroup
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Setup screen for the app
 * This is a container for the different setup steps
 */
@Composable
fun SetupScreen(
    viewModel: SetupViewModel = viewModel(),
    setupStep: SetupStep = SetupStep.WELCOME,
    onNavigateToContacts: () -> Unit = {},
    onNavigateToVoiceTrigger: () -> Unit = {},
    onFinishSetup: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val setupState by viewModel.setupState.collectAsState()

    when (setupStep) {
        SetupStep.WELCOME -> WelcomeScreen(
            onNavigateNext = { onNavigateToContacts() },
            onSkipSetup = { onFinishSetup() }
        )

        SetupStep.PERMISSIONS -> PermissionScreen(
            viewModel = viewModel,
            onNavigateNext = { onNavigateToVoiceTrigger() },
            onNavigateBack = { onNavigateBack() }
        )

        SetupStep.CONTACTS -> ContactsSetupScreen(
            viewModel = viewModel,
            onNavigateNext = { onNavigateToVoiceTrigger() },
            onNavigateBack = { onNavigateBack() }
        )

        SetupStep.VOICE_TRIGGER -> VoiceTriggerSetupScreen(
            viewModel = viewModel,
            onFinishSetup = {
                viewModel.completeSetup()
                onFinishSetup()
            },
            onNavigateBack = { onNavigateBack() }
        )
    }
}

/**
 * Welcome screen for the app
 */
@Composable
fun WelcomeScreen(
    onNavigateNext: () -> Unit = {},
    onSkipSetup: () -> Unit = {}
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.welcome_message),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.app_description),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigateNext() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.continue_setup_button))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onSkipSetup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.skip_setup_button))
            }
        }
    }
}

/**
 * Contacts setup screen
 */
@Composable
fun ContactsSetupScreen(
    viewModel: SetupViewModel,
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    ContactsSetupScreenImpl(
        viewModel = viewModel,
        onNavigateNext = onNavigateNext,
        onNavigateBack = onNavigateBack
    )
}

/**
 * Voice trigger setup screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceTriggerSetupScreen(
    viewModel: SetupViewModel,
    onFinishSetup: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val setupState by viewModel.setupState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for the voice trigger phrase
    var voiceTriggerPhrase by remember { mutableStateOf(setupState.voiceTriggerPhrase) }
    var voiceTriggerEnabled by remember { mutableStateOf(setupState.voiceTriggerEnabled) }

    // State for recording
    var isRecording by remember { mutableStateOf(false) }
    var recordedText by remember { mutableStateOf("") }

    // Check if microphone permission is granted
    val hasMicrophonePermission = PermissionUtils.hasPermission(
        context, android.Manifest.permission.RECORD_AUDIO
    )

    // Speech recognizer
    val speechRecognizer = remember {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    // Speech recognition listener
    val recognitionListener = remember {
        object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isRecording = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isRecording = false
            }

            override fun onError(error: Int) {
                isRecording = false
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = "Error recognizing speech: ${getErrorText(error)}"
                    )
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    val text = matches[0]
                    recordedText = text
                    voiceTriggerPhrase = text
                }
                isRecording = false
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    recordedText = matches[0]
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    // Function to start speech recognition
    fun startSpeechRecognition() {
        if (!hasMicrophonePermission) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Microphone permission is required for voice recognition"
                )
            }
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your emergency trigger phrase")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        try {
            recordedText = ""
            speechRecognizer.setRecognitionListener(recognitionListener)
            speechRecognizer.startListening(intent)
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = "Error starting speech recognition: ${e.message}"
                )
            }
        }
    }

    // Clean up the speech recognizer when the screen is closed
    DisposableEffect(Unit) {
        onDispose {
            speechRecognizer.destroy()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Trigger Setup") },
                navigationIcon = {
                    IconButton(onClick = { onNavigateBack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (!hasMicrophonePermission) {
            // Show permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Microphone Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = PermissionUtils.getPermissionRationale(
                        context, PermissionGroup.MICROPHONE
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        PermissionUtils.openAppSettings(context)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Open Settings")
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onFinishSetup() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip (Not Recommended)")
                }
            }
        } else {
            // Show voice trigger setup UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Set Up Voice Trigger",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your voice trigger phrase will activate the SOS alert when detected, even if your phone is locked.",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Enable/disable voice trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable Voice Trigger",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f)
                    )

                    Switch(
                        checked = voiceTriggerEnabled,
                        onCheckedChange = { enabled ->
                            voiceTriggerEnabled = enabled
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice trigger phrase input
                OutlinedTextField(
                    value = voiceTriggerPhrase,
                    onValueChange = { voiceTriggerPhrase = it },
                    label = { Text("Voice Trigger Phrase") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = voiceTriggerEnabled,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Record button
                Button(
                    onClick = { startSpeechRecognition() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = voiceTriggerEnabled && !isRecording
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Record"
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isRecording) "Listening..." else "Record Phrase")
                    }
                }

                if (isRecording) {
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Listening...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (recordedText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Recorded: $recordedText",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { onNavigateBack() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.weight(0.2f))

                    Button(
                        onClick = {
                            // Save voice trigger settings
                            viewModel.updateVoiceTriggerSettings(
                                enabled = voiceTriggerEnabled,
                                phrase = voiceTriggerPhrase
                            )
                            onFinishSetup()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Finish Setup")
                    }
                }
            }
        }
    }
}

/**
 * Get error text for speech recognition errors
 */
private fun getErrorText(errorCode: Int): String {
    return when (errorCode) {
        SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
        SpeechRecognizer.ERROR_CLIENT -> "Client side error"
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
        SpeechRecognizer.ERROR_NETWORK -> "Network error"
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
        SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy"
        SpeechRecognizer.ERROR_SERVER -> "Server error"
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
        else -> "Unknown error"
    }
}

/**
 * Enum representing the different setup steps
 */
enum class SetupStep {
    WELCOME,
    PERMISSIONS,
    CONTACTS,
    VOICE_TRIGGER
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    CipherTriggerTheme {
        WelcomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun PermissionScreenPreview() {
    CipherTriggerTheme {
        PermissionScreen()
    }
}
