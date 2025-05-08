package com.cite012a_cs32s1.ciphertrigger

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.cite012a_cs32s1.ciphertrigger.navigation.AppNavigation
import com.cite012a_cs32s1.ciphertrigger.navigation.Screen
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CipherTriggerApp()
        }
    }
}

@Composable
fun CipherTriggerApp() {
    CipherTriggerTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            // For development, you can change the start destination to any screen
            // For production, use Screen.Setup.route as the start destination
            AppNavigation(
                navController = navController,
                startDestination = Screen.Dashboard.route // Change this for testing different screens
            )
        }
    }
}