package com.aliJafari.bbarq.ui.auth
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aliJafari.bbarq.ui.auth.viewmodel.LoginUiState
import com.aliJafari.bbarq.ui.auth.viewmodel.LoginViewModel

class LoginActivity : ComponentActivity() {
    private val vm: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                LoginScreen(vm,this)
            }
        }
    }
}

@Composable
fun LoginScreen(vm: LoginViewModel,context: Context) {
    val state by vm.uiState.collectAsState()
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        when (state) {
            is LoginUiState.EnterPhone -> {
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") })
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.sendOtp(phone) }) { Text("Send Code") }
            }

            is LoginUiState.EnterCode -> {
                OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("SMS Code") })
                Spacer(Modifier.height(12.dp))
                Button(onClick = { vm.verifyOtp(context,code) }) { Text("Verify Code") }
            }

            is LoginUiState.Loading -> {
                CircularProgressIndicator()
            }

            is LoginUiState.Error -> {
                Text((state as LoginUiState.Error).message, color = MaterialTheme.colorScheme.error)
            }

            is LoginUiState.Success -> {
                Text("Login successful. Token: ${(state as LoginUiState.Success).token.take(20)}...")
            }
        }
    }
}
