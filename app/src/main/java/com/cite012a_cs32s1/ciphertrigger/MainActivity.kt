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
        }

        setContent {
            CipherTriggerApp(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)

        // Handle the intent in the Composable
        setContent {
            CipherTriggerApp(intent)
        }
    }
}

@Composable
fun CipherTriggerApp(intent: Intent? = null) {
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

            // For development, you can change the start destination to any screen
            // For production, use Screen.Setup.route as the start destination
            AppNavigation(
                navController = navController,
                startDestination = Screen.Setup.route // Change this for testing different screens
            )
        }
    }
}