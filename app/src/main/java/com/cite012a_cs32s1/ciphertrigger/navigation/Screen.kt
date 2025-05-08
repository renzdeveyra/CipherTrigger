package com.cite012a_cs32s1.ciphertrigger.navigation

/**
 * Screen routes for navigation
 */
sealed class Screen(val route: String) {
    object Setup : Screen("setup")
    object Dashboard : Screen("dashboard")
    object Alert : Screen("alert")
    object AlertSummary : Screen("alert_summary")
    object Settings : Screen("settings")
    
    // Setup sub-screens
    object PermissionsSetup : Screen("setup/permissions")
    object ContactsSetup : Screen("setup/contacts")
    object VoiceTriggerSetup : Screen("setup/voice_trigger")
    
    // Settings sub-screens
    object ContactsSettings : Screen("settings/contacts")
    object VoiceTriggerSettings : Screen("settings/voice_trigger")
    object LocationSettings : Screen("settings/location")
    object NotificationSettings : Screen("settings/notifications")
}
