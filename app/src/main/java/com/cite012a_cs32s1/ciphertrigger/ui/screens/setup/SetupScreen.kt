package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme

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
 * Placeholder for the contacts setup screen
 */
@Composable
fun ContactsSetupScreen(
    viewModel: SetupViewModel,
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // This is a placeholder that will be implemented later
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
                text = "Contacts Setup",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onNavigateNext() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
    }
}

/**
 * Placeholder for the voice trigger setup screen
 */
@Composable
fun VoiceTriggerSetupScreen(
    viewModel: SetupViewModel,
    onFinishSetup: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    // This is a placeholder that will be implemented later
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
                text = "Voice Trigger Setup",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { onFinishSetup() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Finish Setup")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { onNavigateBack() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back")
            }
        }
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
