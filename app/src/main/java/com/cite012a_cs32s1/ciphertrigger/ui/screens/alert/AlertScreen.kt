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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cite012a_cs32s1.ciphertrigger.R
import com.cite012a_cs32s1.ciphertrigger.ui.theme.AlertRed
import com.cite012a_cs32s1.ciphertrigger.ui.theme.CipherTriggerTheme
import kotlinx.coroutines.delay

/**
 * Alert screen shown when an SOS alert is triggered
 */
@Composable
fun AlertScreen(
    viewModel: AlertViewModel = viewModel(),
    onAlertComplete: (String) -> Unit = {},
    onAlertCancel: () -> Unit = {}
) {
    val alertState by viewModel.alertState.collectAsState()

    // Initialize the alert when the screen is first displayed
    LaunchedEffect(key1 = Unit) {
        viewModel.initializeAlert()
    }

    // Countdown timer
    LaunchedEffect(key1 = Unit) {
        while (alertState.countdownSeconds > 0 && !alertState.alertSent) {
            delay(1000)
            viewModel.decrementCountdown()
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
            if (!alertState.alertSent) {
                Text(
                    text = stringResource(R.string.alert_title),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.sending_alert_message),
                    color = Color.White,
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${alertState.countdownSeconds}",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        viewModel.cancelAlert()
                        onAlertCancel()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlertRed
                    )
                ) {
                    Text(stringResource(R.string.cancel_button))
                }
            } else {
                Text(
                    text = stringResource(R.string.alert_sent_message),
                    color = Color.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.contacts_notified_message),
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
                        onClick = {
                            viewModel.cancelAlert()
                            onAlertCancel()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AlertRed
                        )
                    ) {
                        Text(stringResource(R.string.cancel_button))
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            val alertId = viewModel.completeAlert()
                            if (alertId != null) {
                                onAlertComplete(alertId)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = AlertRed
                        )
                    ) {
                        Text(stringResource(R.string.complete_button))
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
        // For preview purposes, we're not using the actual ViewModel
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
                // Preview the countdown state
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
                    text = "3",
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlertRed
                    )
                ) {
                    Text("CANCEL")
                }
            }
        }
    }
}
