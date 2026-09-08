package com.shobhankarthish.pocket

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.IntentCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shobhankarthish.pocket.ui.shelf.ShelfScreen
import com.shobhankarthish.pocket.ui.shelf.ShelfViewModel
import com.shobhankarthish.pocket.ui.theme.DarkBackground
import com.shobhankarthish.pocket.ui.theme.LightBackground
import com.shobhankarthish.pocket.ui.theme.PocketTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val pendingShare = MutableStateFlow<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                LightBackground.toArgb(),
                DarkBackground.toArgb(),
            ),
            navigationBarStyle = SystemBarStyle.auto(
                LightBackground.toArgb(),
                DarkBackground.toArgb(),
            ),
        )
        pendingShare.value = peekShare(intent)
        setContent {
            PocketTheme {
                val viewModel: ShelfViewModel = viewModel()
                val share by pendingShare.collectAsStateWithLifecycle()
                LaunchedEffect(share) {
                    val uri = share ?: return@LaunchedEffect
                    viewModel.ingestSuspending(uri)
                    dropShare(intent)
                    pendingShare.value = null
                }
                ShelfScreen(viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingShare.value = peekShare(intent)
    }

    private fun peekShare(intent: Intent?): Uri? {
        if (intent?.action != Intent.ACTION_SEND) return null
        return IntentCompat.getParcelableExtra(intent, Intent.EXTRA_STREAM, Uri::class.java)
    }

    private fun dropShare(intent: Intent) {
        intent.action = Intent.ACTION_MAIN
        intent.removeExtra(Intent.EXTRA_STREAM)
    }
}
