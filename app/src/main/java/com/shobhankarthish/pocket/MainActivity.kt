package com.shobhankarthish.pocket

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shobhankarthish.pocket.bubble.BubbleService
import com.shobhankarthish.pocket.bubble.OverlayPermission
import com.shobhankarthish.pocket.ui.overlay.OverlaySetupScreen
import com.shobhankarthish.pocket.ui.shelf.ShelfScreen
import com.shobhankarthish.pocket.ui.shelf.ShelfViewModel
import com.shobhankarthish.pocket.ui.theme.DarkBackground
import com.shobhankarthish.pocket.ui.theme.LightBackground
import com.shobhankarthish.pocket.ui.theme.PocketTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val pendingShare = MutableStateFlow<IncomingShare?>(null)
    private val overlayAllowed = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applySystemBars(dark = false)
        overlayAllowed.value = OverlayPermission.canDraw(this)
        pendingShare.value = peekShare(intent)
        setContent {
            val viewModel: ShelfViewModel = viewModel()
            val appearance by viewModel.appearance.collectAsStateWithLifecycle()
            val dark = appearance.isDark(isSystemInDarkTheme())
            LaunchedEffect(dark) { applySystemBars(dark) }
            val overlayOk by overlayAllowed.collectAsStateWithLifecycle()
            val promptDismissed by viewModel.overlayPromptDismissed.collectAsStateWithLifecycle()
            val bubbleEnabled by viewModel.bubbleEnabled.collectAsStateWithLifecycle()
            val notify = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission(),
            ) { BubbleService.syncInBackground(this) }
            LaunchedEffect(overlayOk, bubbleEnabled) {
                BubbleService.sync(this@MainActivity)
                if (overlayOk && bubbleEnabled) requestNotifications(notify)
            }
            PocketTheme(darkTheme = dark) {
                if (!overlayOk && !promptDismissed) {
                    OverlaySetupScreen(
                        onAllow = { OverlayPermission.openSettings(this) },
                        onNotNow = {
                            viewModel.dismissOverlayPrompt()
                            viewModel.setBubbleEnabled(false)
                        },
                    )
                } else {
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
                    ShelfScreen(
                        viewModel = viewModel,
                        overlayAllowed = overlayOk,
                        onAllowOverlay = { OverlayPermission.openSettings(this) },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayAllowed.value = OverlayPermission.canDraw(this)
        lifecycleScope.launch { BubbleService.sync(this@MainActivity) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingShare.value = peekShare(intent)
    }

    private fun requestNotifications(
        launcher: androidx.activity.result.ActivityResultLauncher<String>,
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun applySystemBars(dark: Boolean) {
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                LightBackground.toArgb(),
                DarkBackground.toArgb(),
                detectDarkMode = { _ -> dark },
            ),
            navigationBarStyle = SystemBarStyle.auto(
                Color.TRANSPARENT,
                Color.TRANSPARENT,
                detectDarkMode = { _ -> dark },
            ),
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
    }
}
