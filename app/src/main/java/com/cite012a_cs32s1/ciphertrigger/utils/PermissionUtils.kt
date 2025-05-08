package com.cite012a_cs32s1.ciphertrigger.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.core.content.ContextCompat
import com.cite012a_cs32s1.ciphertrigger.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Utility class for handling permissions
 */
object PermissionUtils {
    
    /**
     * All permissions required by the app
     */
    val requiredPermissions = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.SEND_SMS,
        Manifest.permission.CALL_PHONE
    )
    
    /**
     * Check if all required permissions are granted
     */
    fun hasAllRequiredPermissions(context: Context): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Check if a specific permission is granted
     */
    fun hasPermission(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }
    
    /**
     * Get all permissions that are not granted
     */
    fun getMissingPermissions(context: Context): List<String> {
        return requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED
        }
    }
    
    /**
     * Get the permission group for a permission
     */
    fun getPermissionGroup(permission: String): PermissionGroup {
        return when {
            permission == Manifest.permission.ACCESS_FINE_LOCATION || 
            permission == Manifest.permission.ACCESS_COARSE_LOCATION -> PermissionGroup.LOCATION
            
            permission == Manifest.permission.READ_CONTACTS -> PermissionGroup.CONTACTS
            
            permission == Manifest.permission.RECORD_AUDIO -> PermissionGroup.MICROPHONE
            
            permission == Manifest.permission.SEND_SMS -> PermissionGroup.SMS
            
            permission == Manifest.permission.CALL_PHONE -> PermissionGroup.PHONE
            
            else -> PermissionGroup.OTHER
        }
    }
    
    /**
     * Get the rationale for a permission group
     */
    fun getPermissionRationale(context: Context, permissionGroup: PermissionGroup): String {
        return when (permissionGroup) {
            PermissionGroup.LOCATION -> context.getString(R.string.location_permission_rationale)
            PermissionGroup.CONTACTS -> context.getString(R.string.contacts_permission_rationale)
            PermissionGroup.MICROPHONE -> context.getString(R.string.microphone_permission_rationale)
            PermissionGroup.SMS -> context.getString(R.string.sms_permission_rationale)
            PermissionGroup.PHONE -> context.getString(R.string.phone_permission_rationale)
            PermissionGroup.OTHER -> "This permission is required for the app to function properly."
        }
    }
    
    /**
     * Show a snackbar with a rationale and a button to open settings
     */
    fun showPermissionSnackbar(
        scope: CoroutineScope,
        snackbarHostState: SnackbarHostState,
        context: Context,
        permissionGroup: PermissionGroup,
        onOpenSettings: () -> Unit
    ) {
        val rationale = getPermissionRationale(context, permissionGroup)
        
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = rationale,
                actionLabel = "Settings",
                duration = SnackbarDuration.Long
            )
            
            if (result == SnackbarResult.ActionPerformed) {
                onOpenSettings()
            }
        }
    }
    
    /**
     * Open the app settings
     */
    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}

/**
 * Enum representing permission groups
 */
enum class PermissionGroup {
    LOCATION,
    CONTACTS,
    MICROPHONE,
    SMS,
    PHONE,
    OTHER
}
