package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme

/**
 * Setup screen for the app
 * This is a placeholder that will be expanded with actual setup functionality
 */
@Composable
fun SetupScreen(
    onNavigateToContacts: () -> Unit = {},
    onNavigateToVoiceTrigger: () -> Unit = {},
    onFinishSetup: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
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
                text = "Welcome to CipherTrigger",
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Your personal SOS alert system",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { onNavigateToContacts() }
            ) {
                Text("Continue Setup")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { onFinishSetup() }
            ) {
                Text("Skip Setup (Debug)")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SetupScreenPreview() {
    CipherTriggerTheme {
        SetupScreen()
    }
}
