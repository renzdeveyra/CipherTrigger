package com.cite012a_cs32s1.ciphertrigger.ui.screens.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cite012a_cs32s1.ciphertrigger.ui.theme.AlertRed
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import kotlinx.coroutines.delay
import java.util.UUID

/**
 * Alert screen shown when an SOS alert is triggered
 */
@Composable
fun AlertScreen(
    onAlertComplete: (String) -> Unit = {},
    onAlertCancel: () -> Unit = {}
) {
    var countdown by remember { mutableIntStateOf(5) }
    var alertSent by remember { mutableStateOf(false) }
    val alertId = remember { UUID.randomUUID().toString() }
    
    LaunchedEffect(key1 = Unit) {
        // Countdown timer
        while (countdown > 0 && !alertSent) {
            delay(1000)
            countdown--
        }
        
        if (countdown == 0 && !alertSent) {
            alertSent = true
            // In a real app, we would send the alert here
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AlertRed)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!alertSent) {
                Text(
                    text = "SOS ALERT",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Sending alert in",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "$countdown",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { onAlertCancel() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlertRed
                    )
                ) {
                    Text("CANCEL")
                }
            } else {
                Text(
                    text = "ALERT SENT",
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Your emergency contacts have been notified",
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onAlertCancel() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AlertRed
                        )
                    ) {
                        Text("CANCEL")
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Button(
                        onClick = { onAlertComplete(alertId) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AlertRed
                        )
                    ) {
                        Text("COMPLETE")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AlertScreenPreview() {
    CipherTriggerTheme {
        AlertScreen()
    }
}
