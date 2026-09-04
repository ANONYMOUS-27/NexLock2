package com.nexlock.security

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.nexlock.security.core.SecurityStore

class LockActivity : ComponentActivity() {
    private lateinit var prompt: BiometricPrompt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = SecurityStore(this)
        prompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this), object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { finish() }
        })
        setContent { LockScreen(store) { launchBiometric() } }
    }

    private fun launchBiometric() {
        val allowed = BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Desbloquear aplicativo")
            .setSubtitle("Confirme sua identidade para continuar")
            .setAllowedAuthenticators(allowed)
            .build()
        prompt.authenticate(info)
    }
}

@Composable
private fun LockScreen(store: SecurityStore, biometric: () -> Unit) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NexLock", style = MaterialTheme.typography.headlineLarge)
            Text("Este aplicativo está protegido")
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(value = pin, onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it }, label = { Text("PIN") }, visualTransformation = PasswordVisualTransformation())
            if (error) Text("PIN incorreto")
            Spacer(Modifier.height(12.dp))
            Button(onClick = { if (store.verifyPin(pin)) (LocalContext.current as? ComponentActivity)?.finish() else error = true }) { Text("Desbloquear") }
            TextButton(onClick = biometric) { Text("Usar biometria") }
        }
    }
}
