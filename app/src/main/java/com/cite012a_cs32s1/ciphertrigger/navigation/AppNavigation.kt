package com.cite012a_cs32s1.ciphertrigger.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.cite012a_cs32s1.ciphertrigger.ui.screens.dashboard.DashboardScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.alert.AlertScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.alert.AlertSummaryScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.settings.ContactsSettingsScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.settings.SettingsScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.setup.SetupScreen
import com.cite012a_cs32s1.ciphertrigger.ui.screens.setup.SetupStep

/**
 * Main navigation component for the app
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Setup.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Setup flow
        navigation(
            startDestination = Screen.WelcomeSetup.route,
            route = Screen.Setup.route
        ) {
            composable(Screen.WelcomeSetup.route) {
                SetupScreen(
                    setupStep = SetupStep.WELCOME,
                    onNavigateToContacts = {
                        navController.navigate(Screen.PermissionsSetup.route)
                    },
                    onFinishSetup = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.PermissionsSetup.route) {
                SetupScreen(
                    setupStep = SetupStep.PERMISSIONS,
                    onNavigateToVoiceTrigger = {
                        navController.navigate(Screen.ContactsSetup.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ContactsSetup.route) {
                SetupScreen(
                    setupStep = SetupStep.CONTACTS,
                    onNavigateToVoiceTrigger = {
                        navController.navigate(Screen.VoiceTriggerSetup.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.VoiceTriggerSetup.route) {
                SetupScreen(
                    setupStep = SetupStep.VOICE_TRIGGER,
                    onFinishSetup = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Setup.route) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }

        // Main screens
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onTriggerAlert = {
                    navController.navigate(Screen.Alert.route)
                }
            )
        }

        composable(Screen.Alert.route) {
            AlertScreen(
                onAlertComplete = { alertId ->
                    navController.navigate("${Screen.AlertSummary.route}/$alertId") {
                        popUpTo(Screen.Dashboard.route)
                    }
                },
                onAlertCancel = {
                    navController.popBackStack()
                }
            )
        }

        composable("${Screen.AlertSummary.route}/{alertId}") { backStackEntry ->
            val alertId = backStackEntry.arguments?.getString("alertId")
            AlertSummaryScreen(
                alertId = alertId,
                onNavigateHome = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        // Settings screens
        navigation(
            startDestination = Screen.Settings.route,
            route = "settings_flow"
        ) {
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onNavigateToContactsSettings = {
                        navController.navigate(Screen.ContactsSettings.route)
                    },
                    onNavigateToVoiceTriggerSettings = {
                        navController.navigate(Screen.VoiceTriggerSettings.route)
                    },
                    onNavigateToLocationSettings = {
                        navController.navigate(Screen.LocationSettings.route)
                    },
                    onNavigateToNotificationSettings = {
                        navController.navigate(Screen.NotificationSettings.route)
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.ContactsSettings.route) {
                ContactsSettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.VoiceTriggerSettings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.LocationSettings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.NotificationSettings.route) {
                SettingsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
