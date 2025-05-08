package com.cite012a_cs32s1.ciphertrigger.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import com.cite012a_cs32s1.ciphertrigger.ui.theme.SuccessGreen

/**
 * Status indicator component for showing service status
 */
@Composable
fun StatusIndicator(
    icon: ImageVector,
    title: String,
    isActive: Boolean,
    onToggle: ((Boolean) -> Unit)? = null
) {
    var active by remember { mutableStateOf(isActive) }
    
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        
        if (active) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Active",
                tint = SuccessGreen,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Inactive",
                tint = Color.Gray,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Switch(
            checked = active,
            onCheckedChange = { newValue ->
                active = newValue
                onToggle?.invoke(newValue)
            }
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
            isActive = true
        )
    }
}
