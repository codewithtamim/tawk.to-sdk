package io.github.codewithtamim.tawktoandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import io.github.codewithtamim.tawktoandroid.sdk.TawkToChat
import io.github.codewithtamim.tawktoandroid.ui.theme.TawktoAndroidSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TawktoAndroidSDKTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerpadding ->
                    TawkToChat(
                        chatUrl = "https://tawk.to/chat/YOUR_PROPERTY_ID/YOUR_WIDGET_ID",
                        modifier = Modifier.padding(innerpadding)
                    )
                }

            }
        }
    }
}