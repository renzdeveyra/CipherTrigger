package com.cite012a_cs32s1.ciphertrigger

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.cite012a_cs32s1.ciphertrigger.di.AppModule
import com.cite012a_cs32s1.ciphertrigger.navigation.AppNavigation
import com.cite012a_cs32s1.ciphertrigger.navigation.Screen
import com.cite012a_cs32s1.ciphertrigger.services.VoiceRecognitionManager
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val preferencesRepository by lazy { AppModule.providePreferencesRepository(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize voice recognition service
        lifecycleScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            if (preferences.isSetupCompleted && preferences.voiceTriggerEnabled) {
                VoiceRecognitionManager.initialize(this@MainActivity, preferencesRepository)
            }

            // Set content after checking preferences to determine start destination
            setContent {
                CipherTriggerApp(
                    intent = intent,
                    isSetupCompleted = preferences.isSetupCompleted
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle the intent in the Composable
        lifecycleScope.launch {
            val preferences = preferencesRepository.userPreferencesFlow.first()
            setContent {
                CipherTriggerApp(
                    intent = intent,
                    isSetupCompleted = preferences.isSetupCompleted
                )
            }
        }
    }
}

@Composable
fun CipherTriggerApp(
    intent: Intent? = null,
    isSetupCompleted: Boolean = false
) {
    CipherTriggerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()
            val context = LocalContext.current

            // Handle intent actions (e.g., voice trigger)
            LaunchedEffect(intent) {
                intent?.let {
                    when (it.action) {
                        "com.cite012a_cs32s1.ciphertrigger.action.TRIGGER_ALERT" -> {
                            // Navigate to the alert screen
                            navController.navigate(Screen.Alert.route) {
                                popUpTo(navController.graph.startDestinationId)
                            }
                        }
                    }
                }
            }

            // Determine start destination based on setup completion status
            val startDestination = if (isSetupCompleted) {
                Screen.Dashboard.route
            } else {
                Screen.Setup.route
            }

            AppNavigation(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}