package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import android.Manifest
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.ui.components.PermissionCard
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionGroup
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Screen for requesting permissions during setup
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionScreen(
    viewModel: SetupViewModel = viewModel(),
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val setupState by viewModel.setupState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Create permission states for each permission group
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    
    val contactsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.READ_CONTACTS
        )
    )
    
    val microphonePermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO
        )
    )
    
    val smsPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.SEND_SMS
        )
    )
    
    val phonePermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CALL_PHONE
        )
    )
    
    // Check permissions when the screen is first displayed
    LaunchedEffect(key1 = Unit) {
        viewModel.checkPermissions()
    }
    
    // Update the ViewModel when permissions change
    LaunchedEffect(
        key1 = locationPermissionsState.allPermissionsGranted,
        key2 = contactsPermissionState.allPermissionsGranted
    ) {
        viewModel.checkPermissions()
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "CipherTrigger needs the following permissions to function properly:",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Location permission
            PermissionCard(
                title = "Location",
                description = stringResource(R.string.location_permission_rationale),
                isGranted = locationPermissionsState.allPermissionsGranted,
                onRequestPermission = {
                    locationPermissionsState.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    PermissionUtils.openAppSettings(context)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Contacts permission
            PermissionCard(
                title = "Contacts",
                description = stringResource(R.string.contacts_permission_rationale),
                isGranted = contactsPermissionState.allPermissionsGranted,
                onRequestPermission = {
                    contactsPermissionState.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    PermissionUtils.openAppSettings(context)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Microphone permission
            PermissionCard(
                title = "Microphone",
                description = stringResource(R.string.microphone_permission_rationale),
                isGranted = microphonePermissionState.allPermissionsGranted,
                onRequestPermission = {
                    microphonePermissionState.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    PermissionUtils.openAppSettings(context)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // SMS permission
            PermissionCard(
                title = "SMS",
                description = stringResource(R.string.sms_permission_rationale),
                isGranted = smsPermissionState.allPermissionsGranted,
                onRequestPermission = {
                    smsPermissionState.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    PermissionUtils.openAppSettings(context)
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone permission
            PermissionCard(
                title = "Phone",
                description = stringResource(R.string.phone_permission_rationale),
                isGranted = phonePermissionState.allPermissionsGranted,
                onRequestPermission = {
                    phonePermissionState.launchMultiplePermissionRequest()
                },
                onOpenSettings = {
                    PermissionUtils.openAppSettings(context)
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Continue button
            Button(
                onClick = { onNavigateNext() },
                modifier = Modifier.fillMaxWidth(),
                enabled = locationPermissionsState.allPermissionsGranted && 
                          contactsPermissionState.allPermissionsGranted
            ) {
                Text("Continue")
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skip button (for development purposes)
            Button(
                onClick = { onNavigateNext() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip Permissions (Not Recommended)")
            }
        }
    }
}
