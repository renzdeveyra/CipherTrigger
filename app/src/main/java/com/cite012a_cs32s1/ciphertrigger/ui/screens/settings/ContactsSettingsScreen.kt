package com.cite012a_cs32s1.ciphertrigger.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.ui.components.ContactSelectionItem
import com.cite012a_cs32s1.ciphertrigger.ui.components.EmergencyContactItem
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.launch

/**
 * Screen for managing emergency contacts in settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {},
    onNavigateToPermissionsSetup: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showContactSelector by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var deviceContacts by remember { mutableStateOf<List<ContactRepository.DeviceContact>>(emptyList()) }

    // Check if contacts permission is granted
    val hasContactsPermission = settingsState.hasContactsPermission

    // Load contacts when the screen is first displayed
    LaunchedEffect(key1 = hasContactsPermission) {
        if (hasContactsPermission && showContactSelector) {
            deviceContacts = viewModel.loadDeviceContacts()
        }
    }

    // Filter contacts based on search query
    val filteredContacts = deviceContacts.filter { contact ->
        contact.name.contains(searchQuery, ignoreCase = true) ||
        contact.phoneNumbers.any { it.contains(searchQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Emergency Contacts") },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!showContactSelector && hasContactsPermission) {
                FloatingActionButton(
                    onClick = {
                        showContactSelector = true
                        scope.launch {
                            deviceContacts = viewModel.loadDeviceContacts()
                        }
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact")
                }
            }
        }
    ) { paddingValues ->
        if (!hasContactsPermission) {
            // Show permission request UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.height(72.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Contacts Permission Required",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "To add emergency contacts, you need to grant permission to access your contacts.",
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
        } else if (showContactSelector) {
            // Show contact selector UI
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

                // Available contacts section
                Text(
                    text = "Available Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(filteredContacts) { contact ->
                        val isSelected = settingsState.emergencyContacts.any {
                            it.name == contact.name && it.phoneNumber in contact.phoneNumbers
                        }

                        ContactSelectionItem(
                            contact = contact,
                            isSelected = isSelected,
                            onSelectContact = { deviceContact, phoneNumber ->
                                if (!isSelected) {
                                    viewModel.addEmergencyContact(
                                        deviceContact = deviceContact,
                                        phoneNumber = phoneNumber,
                                        priority = settingsState.emergencyContacts.size + 1,
                                        sendSms = settingsState.sendSmsDefault,
                                        makeCall = settingsState.makeCallDefault
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Contact added")
                                    }
                                }
                            }
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { showContactSelector = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Done")
                    }
                }
            }
        } else {
            // Show contacts management UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                if (settingsState.emergencyContacts.isEmpty()) {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.height(72.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "No Emergency Contacts",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Add emergency contacts who will be notified when you trigger an SOS alert.",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                showContactSelector = true
                                scope.launch {
                                    deviceContacts = viewModel.loadDeviceContacts()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Contacts")
                        }
                    }
                } else {
                    // Contact list
                    Text(
                        text = "Emergency Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "These contacts will be notified when you trigger an SOS alert.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        val sortedContacts = settingsState.emergencyContacts.sortedBy { it.priority }
                        items(sortedContacts) { contact ->
                            EmergencyContactItem(
                                contact = contact,
                                onRemoveContact = { contactId ->
                                    viewModel.removeEmergencyContact(contactId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Contact removed")
                                    }
                                },
                                onToggleSendSms = { contactId, sendSms ->
                                    viewModel.updateContactSendSms(contactId, sendSms)
                                },
                                onToggleMakeCall = { contactId, makeCall ->
                                    viewModel.updateContactMakeCall(contactId, makeCall)
                                },
                                onMovePriorityUp = { contactId ->
                                    viewModel.moveContactPriorityUp(contactId)
                                },
                                onMovePriorityDown = { contactId ->
                                    viewModel.moveContactPriorityDown(contactId)
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactsSettingsScreenPreview() {
    CipherTriggerTheme {
        ContactsSettingsScreen(onNavigateToPermissionsSetup = {})
    }
}
