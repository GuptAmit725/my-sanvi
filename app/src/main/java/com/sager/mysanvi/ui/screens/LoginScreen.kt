package com.sager.mysanvi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.sager.mysanvi.data.api.ApiManager
import com.sager.mysanvi.data.api.UserStatusResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserStatusResponse) -> Unit
) {
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var currentStep by remember { mutableStateOf(LoginStep.PHONE_INPUT) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "My Sanvi",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Unified access to SaGer & Mandii",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        when (currentStep) {
            LoginStep.PHONE_INPUT -> {
                PhoneInputStep(
                    phone = phone,
                    onPhoneChange = { phone = it; errorMessage = null },
                    onSendOtp = {
                        if (phone.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    delay(1000) // Simulate API call delay
                                    errorMessage = null
                                    currentStep = LoginStep.OTP_VERIFICATION
                                } catch (e: Exception) {
                                    errorMessage = "Failed to send OTP"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Please enter your phone number"
                        }
                    },
                    isLoading = isLoading,
                    error = errorMessage
                )
            }
            LoginStep.OTP_VERIFICATION -> {
                OtpVerificationStep(
                    phone = phone,
                    otp = otp,
                    onOtpChange = { otp = it; errorMessage = null },
                    onVerifyOtp = {
                        if (otp.isNotBlank()) {
                            isLoading = true
                            scope.launch {
                                try {
                                    delay(1000) // Simulate network delay
                                    if (otp.length == 6) {
                                        // Check user status using mock API
                                        val userStatus = ApiManager.checkUserStatus(phone)
                                        onLoginSuccess(userStatus)
                                    } else {
                                        errorMessage = "Please enter a 6-digit OTP"
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Verification failed: ${e.message}"
                                } finally {
                                    isLoading = false
                                }
                            }
                        } else {
                            errorMessage = "Please enter the verification code"
                        }
                    },
                    onResendOtp = {
                        scope.launch {
                            try {
                                delay(500)
                                errorMessage = "OTP resent! (Demo: use any 6-digit code)"
                            } catch (e: Exception) {
                                errorMessage = "Failed to resend OTP"
                            }
                        }
                    },
                    onChangePhone = {
                        currentStep = LoginStep.PHONE_INPUT
                        otp = ""
                        errorMessage = null
                    },
                    isLoading = isLoading,
                    error = errorMessage
                )
            }
        }
    }
}

@Composable
private fun PhoneInputStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter your phone number",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            placeholder = { Text("+91 9876543210") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSendOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && phone.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Send OTP")
        }
    }
}

@Composable
private fun OtpVerificationStep(
    phone: String,
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onChangePhone: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Enter verification code",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "We sent a code to $phone",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = otp,
            onValueChange = onOtpChange,
            label = { Text("OTP") },
            placeholder = { Text("123456") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        if (error != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerifyOtp,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading && otp.isNotBlank()
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Verify & Continue")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton(
                onClick = onChangePhone,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Change Phone")
            }

            TextButton(
                onClick = onResendOtp,
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            ) {
                Text("Resend OTP")
            }
        }
    }
}

enum class LoginStep {
    PHONE_INPUT,
    OTP_VERIFICATION
}