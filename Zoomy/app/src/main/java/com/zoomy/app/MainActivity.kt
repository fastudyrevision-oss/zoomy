package com.zoomy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.zoomy.app.navigation.ZoomyNavHost
import com.zoomy.app.ui.AppViewModel
import com.zoomy.app.ui.GameViewModel
import com.zoomy.app.ui.components.ZoomySnackbarHost
import com.zoomy.app.ui.theme.ZoomyTheme
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels {
        AppViewModel.provideFactory((application as ZoomyApp).container)
    }

    private val gameViewModel: GameViewModel by viewModels {
        GameViewModel.provideFactory((application as ZoomyApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Consent request on app start
        val container = (application as ZoomyApp).container
        container.consentManager.requestConsentAndInit(this) {
            container.adsManager.preloadAds()
        }

        setContent {
            var currentSnackbarMessage by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(Unit) {
                container.snackbarMessages.collectLatest { msg ->
                    currentSnackbarMessage = msg
                }
            }

            ZoomyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        ZoomyNavHost(
                            appViewModel = appViewModel,
                            gameViewModel = gameViewModel
                        )
                        ZoomySnackbarHost(
                            message = currentSnackbarMessage,
                            onDismiss = { currentSnackbarMessage = null }
                        )
                    }
                }
            }
        }
    }
}
