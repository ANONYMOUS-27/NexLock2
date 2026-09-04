package com.nexlock.security

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.nexlock.security.core.AppMonitorService
import com.nexlock.security.core.SecurityStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { NexLockHome(this) }
    }
}

@Composable
fun NexLockHome(context: Context) {
    val store = remember { SecurityStore(context) }
    var pin by remember { mutableStateOf("") }
    var refresh by remember { mutableIntStateOf(0) }
    val apps = remember(refresh) {
        context.packageManager.queryIntentActivities(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0)
            .map { it.activityInfo.packageName to it.loadLabel(context.packageManager).toString() }
            .filter { it.first != context.packageName }
            .distinctBy { it.first }
            .sortedBy { it.second.lowercase() }
    }

    MaterialTheme {
        Scaffold(topBar = { TopAppBar(title = { Text("NexLock") }) }) { pad ->
            LazyColumn(Modifier.padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Text("Proteção de aplicativos", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = pin, onValueChange = { if (it.length <= 8 && it.all(Char::isDigit)) pin = it }, label = { Text(if (store.hasPin()) "Novo PIN" else "Crie um PIN") })
                    Button(onClick = { if (pin.length >= 4) { store.setPin(pin); pin = "" } }, enabled = pin.length >= 4) { Text("Salvar PIN") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text("Conceder acesso de uso") }
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { context.startForegroundService(Intent(context, AppMonitorService::class.java)) }) { Text("Ativar proteção") }
                    Spacer(Modifier.height(16.dp))
                    Text("Escolha os aplicativos que exigirão autenticação:")
                }
                items(apps) { (pkg, label) ->
                    val checked = store.protectedPackages().contains(pkg)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(label, modifier = Modifier.weight(1f))
                        Switch(checked = checked, onCheckedChange = { store.setProtected(pkg, it); refresh++ })
                    }
                }
            }
        }
    }
}
