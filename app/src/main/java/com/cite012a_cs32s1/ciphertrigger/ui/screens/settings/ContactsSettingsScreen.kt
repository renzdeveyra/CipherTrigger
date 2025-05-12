package com.cite012a_cs32s1.ciphertrigger.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.data.models.EmergencyContact
import com.cite012a_cs32s1.ciphertrigger.data.repositories.ContactRepository
import com.cite012a_cs32s1.ciphertrigger.ui.components.ContactSelectionItem
import com.cite012a_cs32s1.ciphertrigger.utils.PermissionUtils
import kotlinx.coroutines.launch

/**
 * Screen for managing emergency contacts in settings
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsSettingsScreen(
    viewModel: SettingsViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val settingsState by viewModel.settingsState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showContactSelection by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var deviceContacts by remember { mutableStateOf<List<ContactRepository.DeviceContact>>(emptyList()) }

    // Check if contacts permission is granted
    val hasContactsPermission = PermissionUtils.hasPermission(
        context, android.Manifest.permission.READ_CONTACTS
    )

    // Load device contacts when needed
    LaunchedEffect(key1 = showContactSelection, key2 = hasContactsPermission) {
        if (showContactSelection && hasContactsPermission) {
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
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (!showContactSelection) {
                FloatingActionButton(
                    onClick = { showContactSelection = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact"
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Global alert method preference
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Alert Method Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Send SMS by default",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = settingsState.sendSmsDefault,
                            onCheckedChange = { viewModel.updateSendSmsDefault(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Make calls by default",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )

                        Switch(
                            checked = settingsState.makeCallDefault,
                            onCheckedChange = { viewModel.updateMakeCallDefault(it) }
                        )
                    }
                }
            }

            if (showContactSelection) {
                // Contact selection view
                Column(
                    modifier = Modifier.fillMaxSize()
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

                    // Available contacts
                    Text(
                        text = "Available Contacts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    if (!hasContactsPermission) {
                        Text(
                            text = "Contacts permission is required to access your contacts",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    } else if (filteredContacts.isEmpty()) {
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
                                .weight(1f)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(filteredContacts) { contact ->
                                val isSelected = settingsState.emergencyContacts.any {
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
                                            priority = settingsState.emergencyContacts.size + 1,
                                            sendSms = settingsState.sendSmsDefault,
                                            makeCall = settingsState.makeCallDefault
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

                    // Done button
                    Button(
                        onClick = { showContactSelection = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        Text("Done")
                    }
                }
            } else {
                // Emergency contacts list
                Text(
                    text = "Your Emergency Contacts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (settingsState.emergencyContacts.isEmpty()) {
                    Text(
                        text = "You haven't added any emergency contacts yet",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(settingsState.emergencyContacts.sortedBy { it.priority }) { contact ->
                            EmergencyContactSettingsItem(
                                contact = contact,
                                onRemoveContact = { contactId ->
                                    viewModel.removeEmergencyContact(contactId)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "${contact.name} removed from emergency contacts"
                                        )
                                    }
                                },
                                onUpdateSendSms = { contactId, sendSms ->
                                    viewModel.updateContactSendSms(contactId, sendSms)
                                },
                                onUpdateMakeCall = { contactId, makeCall ->
                                    viewModel.updateContactMakeCall(contactId, makeCall)
                                },
                                onMovePriorityUp = { contactId ->
                                    viewModel.moveContactPriorityUp(contactId)
                                },
                                onMovePriorityDown = { contactId ->
                                    viewModel.moveContactPriorityDown(contactId)
                                },
                                isFirst = contact.priority <= 1,
                                isLast = contact.priority >= settingsState.emergencyContacts.size
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Component for displaying and configuring an emergency contact in settings
 */
@Composable
fun EmergencyContactSettingsItem(
    contact: EmergencyContact,
    onRemoveContact: (String) -> Unit = {},
    onUpdateSendSms: (String, Boolean) -> Unit = { _, _ -> },
    onUpdateMakeCall: (String, Boolean) -> Unit = { _, _ -> },
    onMovePriorityUp: (String) -> Unit = {},
    onMovePriorityDown: (String) -> Unit = {},
    isFirst: Boolean = false,
    isLast: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Contact info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Contact",
                    modifier = Modifier.padding(4.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = contact.phoneNumber,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Text(
                        text = "Priority: ${contact.priority}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Priority controls
                Column {
                    IconButton(
                        onClick = { onMovePriorityUp(contact.id) },
                        enabled = !isFirst
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Move Up",
                            tint = if (isFirst) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                  else MaterialTheme.colorScheme.primary
                        )
                    }

                    IconButton(
                        onClick = { onMovePriorityDown(contact.id) },
                        enabled = !isLast
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Move Down",
                            tint = if (isLast) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                                  else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Alert options
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // SMS option
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "SMS",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = contact.sendSms,
                        onCheckedChange = { onUpdateSendSms(contact.id, it) }
                    )
                }

                // Call option
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "Call",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Switch(
                        checked = contact.makeCall,
                        onCheckedChange = { onUpdateMakeCall(contact.id, it) }
                    )
                }
            }

            // Remove button
            Button(
                onClick = { onRemoveContact(contact.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Remove Contact")
            }
        }
    }
}
