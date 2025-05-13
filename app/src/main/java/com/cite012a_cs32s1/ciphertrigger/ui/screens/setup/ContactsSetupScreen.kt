package com.cite012a_cs32s1.ciphertrigger.ui.screens.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.ui.components.ContactSelectionItem
import com.cite012a_cs32s1.ciphertrigger.ui.components.EmergencyContactItem
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionGroup
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.launch
import androidx.compose.ui.tooling.preview.Preview
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme

/**
 * Screen for selecting emergency contacts during setup
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSetupScreenImpl(
    viewModel: SetupViewModel,
    onNavigateNext: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    onFinishSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val setupState by viewModel.setupState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }

    // Check if contacts permission is granted
    val hasContactsPermission = PermissionUtils.hasPermission(
        context, android.Manifest.permission.READ_CONTACTS
    )

    // Load contacts when the screen is first displayed
    LaunchedEffect(key1 = hasContactsPermission) {
        if (hasContactsPermission) {
            viewModel.loadDeviceContacts()
        }
    }

    // Filter contacts based on search query
    val filteredContacts = setupState.deviceContacts.filter { contact ->
        contact.name.contains(searchQuery, ignoreCase = true) ||
        contact.phoneNumbers.any { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Emergency Contacts") },
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
        if (!hasContactsPermission) {
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
                    text = "Contacts Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = PermissionUtils.getPermissionRationale(
                        context, PermissionGroup.CONTACTS
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
                    onClick = {
                        viewModel.completeSetup()
                        onFinishSetup()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip (Not Recommended)")
                }
            }
        } else {
            // Show contacts selection UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search contacts") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true
                )

                // Selected contacts section
                if (setupState.selectedContacts.isNotEmpty()) {
                    Text(
                        text = "Selected Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .weight(0.4f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(setupState.selectedContacts) { contact ->
                            EmergencyContactItem(
                                contact = contact,
                                onRemoveContact = { contactId ->
                                    viewModel.removeEmergencyContact(contactId)
                                }
                            )
                        }
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }

                // Available contacts section
                Text(
                    text = "Available Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (filteredContacts.isEmpty()) {
                    if (searchQuery.isNotEmpty()) {
                        Text(
                            text = "No contacts found matching '$searchQuery'",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else {
                        Text(
                            text = "No contacts found on your device",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(if (setupState.selectedContacts.isEmpty()) 1f else 0.6f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(filteredContacts) { contact ->
                            val isSelected = setupState.selectedContacts.any {
                                it.name == contact.name &&
                                contact.phoneNumbers.contains(it.phoneNumber)
                            }

                            ContactSelectionItem(
                                contact = contact,
                                isSelected = isSelected,
                                onSelectContact = { selectedContact, phoneNumber ->
                                    viewModel.addEmergencyContact(
                                        deviceContact = selectedContact,
                                        phoneNumber = phoneNumber,
                                        priority = setupState.selectedContacts.size + 1,
                                        sendSms = true,
                                        makeCall = setupState.selectedContacts.isEmpty() // Make call for the first contact only
                                    )

                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "${selectedContact.name} added as emergency contact"
                                        )
                                    }
                                }
                            )
                        }
                    }
                }

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
                        onClick = { onNavigateNext() },
                        modifier = Modifier.weight(1f),
                        enabled = setupState.selectedContacts.isNotEmpty()
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsSetupScreenPreview() {
    CipherTriggerTheme {
        // For preview purposes, we're not using the actual ViewModel
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Select Emergency Contacts") },
                    navigationIcon = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
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
                    .padding(horizontal = 16.dp)
            ) {
                // Search field
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search contacts") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    singleLine = true
                )

                // Selected contacts section
                Text(
                    text = "Selected Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Preview selected contacts
                LazyColumn(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(2) { index ->
                        EmergencyContactItem(
                            contact = com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact(
                                id = "$index",
                                name = if (index == 0) "John Doe" else "Jane Smith",
                                phoneNumber = if (index == 0) "+1 (555) 123-4567" else "+1 (555) 987-6543",
                                priority = index + 1,
                                sendSms = true,
                                makeCall = index == 0
                            ),
                            onRemoveContact = { }
                        )
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Available contacts section
                Text(
                    text = "Available Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Preview available contacts
                LazyColumn(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(3) { index ->
                        ContactSelectionItem(
                            contact = ContactRepository.DeviceContact(
                                id = "${index + 3}",
                                name = "Contact ${index + 3}",
                                phoneNumbers = listOf("+1 (555) ${index + 3}00-${index + 3}000"),
                                photoUri = null
                            ),
                            isSelected = false,
                            onSelectContact = { _, _ -> }
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Back")
                    }

                    Spacer(modifier = Modifier.weight(0.2f))

                    Button(
                        onClick = { },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Continue")
                    }
                }
            }
        }
    }
}
