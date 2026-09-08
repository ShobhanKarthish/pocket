package com.shobhankarthish.pocket

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shobhankarthish.pocket.ui.shelf.ShelfScreen
import com.shobhankarthish.pocket.ui.shelf.ShelfViewModel
import com.shobhankarthish.pocket.ui.theme.DarkBackground
import com.shobhankarthish.pocket.ui.theme.LightBackground
import com.shobhankarthish.pocket.ui.theme.PocketTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private val pendingShare = MutableStateFlow<IncomingShare?>(null)

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
                    val incoming = share ?: return@LaunchedEffect
                    when (incoming) {
                        is IncomingShare.Stream -> viewModel.ingestSuspending(incoming.uri)
                        is IncomingShare.Streams -> viewModel.ingestMany(incoming.uris)
                        is IncomingShare.PlainText -> viewModel.ingestTextSuspending(incoming.text)
                    }
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
}
