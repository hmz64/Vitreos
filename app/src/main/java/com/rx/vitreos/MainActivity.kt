package com.rx.vitreos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rx.vitreos.ui.navigation.VitreosNavHost
import com.rx.vitreos.ui.theme.VitreosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VitreosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VitreosNavHost()
                }
            }
        }
    }
}