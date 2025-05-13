package com.cite012a_cs32s1.ciphertrigger.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import com.cite012a_cs32s1.ciphertrigger.ui.theme.SuccessGreen

/**
 * Status indicator component for showing service status
 * The icon is a button that toggles the feature on/off
 * The text is clickable and navigates to the corresponding settings page
 * If isAvailable is false, the icon will be red and unclickable
 */
@Composable
fun StatusIndicator(
    icon: ImageVector,
    title: String,
    isActive: Boolean,
    isAvailable: Boolean = true,
    onToggle: ((Boolean) -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    // We don't need a local state variable as we're using the isActive parameter directly

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAvailable) {
            // Icon button to toggle the feature (only clickable if available)
            IconButton(
                onClick = {
                    val newValue = !isActive
                    onToggle?.invoke(newValue)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (isActive) "Disable $title" else "Enable $title",
                    tint = if (isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Non-clickable icon with red tint when unavailable
            Icon(
                imageVector = icon,
                contentDescription = "$title Unavailable",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Clickable text to navigate to settings
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToSettings() }
        )
    }
}

/**
 * Status indicator component for showing service status with a Painter resource
 * The icon is a button that toggles the feature on/off
 * The text is clickable and navigates to the corresponding settings page
 * If isAvailable is false, the icon will be red and unclickable
 */
@Composable
fun StatusIndicator(
    iconPainter: Painter,
    title: String,
    isActive: Boolean,
    isAvailable: Boolean = true,
    onToggle: ((Boolean) -> Unit)? = null,
    onNavigateToSettings: () -> Unit = {}
) {
    // We don't need a local state variable as we're using the isActive parameter directly

    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isAvailable) {
            // Icon button to toggle the feature (only clickable if available)
            IconButton(
                onClick = {
                    val newValue = !isActive
                    onToggle?.invoke(newValue)
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    painter = iconPainter,
                    contentDescription = if (isActive) "Disable ${title}" else "Enable ${title}",
                    tint = if (isActive) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else {
            // Non-clickable icon with red tint when unavailable
            Icon(
                painter = iconPainter,
                contentDescription = "$title Unavailable",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .size(40.dp)
                    .padding(8.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Clickable text to navigate to settings
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
            modifier = Modifier
                .weight(1f)
                .clickable { onNavigateToSettings() }
        )
    }
}

@Preview
@Composable
fun StatusIndicatorPreview() {
    CipherTriggerTheme {
        StatusIndicator(
            icon = Icons.Default.Check,
            title = "Voice Trigger",
            isActive = true,
            onToggle = {},
            onNavigateToSettings = {}
        )
    }
}
