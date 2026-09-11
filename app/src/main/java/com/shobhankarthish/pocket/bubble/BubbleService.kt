package com.shobhankarthish.pocket.bubble

import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.WindowManager
import android.view.DragEvent
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.shobhankarthish.pocket.MainActivity
import com.shobhankarthish.pocket.PocketApp
import com.shobhankarthish.pocket.R
import com.shobhankarthish.pocket.shelf.BatchTally
import com.shobhankarthish.pocket.shelf.IngestResult
import com.shobhankarthish.pocket.shelf.ItemIngestor
import com.shobhankarthish.pocket.shelf.ShareDecision
import com.shobhankarthish.pocket.shelf.ShareOutcome
import com.shobhankarthish.pocket.shelf.ShareOut
import com.shobhankarthish.pocket.shelf.ShelfItem
import com.shobhankarthish.pocket.shelf.ShelfRepository
import com.shobhankarthish.pocket.shelf.prefs.SettingsPrefs
import com.shobhankarthish.pocket.shelf.toInboundFile
import com.shobhankarthish.pocket.ui.motion.lightHaptic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class BubbleService : LifecycleService() {
    private val container get() = (application as PocketApp).container
    private val repository: ShelfRepository get() = container.repository
    private val ingestor: ItemIngestor get() = container.ingestor
    private val settingsPrefs: SettingsPrefs get() = container.settingsPrefs
    private val windowManager get() = getSystemService(WINDOW_SERVICE) as WindowManager

    private var host: OverlayComposeHost? = null
    private var params: WindowManager.LayoutParams? = null
    private var placement = BubblePlacement(onLeft = false, yFraction = BubbleChrome.DefaultYFraction)
    private var dragging = false
    private var dragX = 0
    private var dragY = 0
    private var statusJob: Job? = null

    private val mode = MutableStateFlow(BubbleMode.Collapsed)
    private val dropHot = MutableStateFlow(false)
    private val status = MutableStateFlow<String?>(null)
    private val count = MutableStateFlow(0)
    private val dark = MutableStateFlow(false)
    private val haptics = MutableStateFlow(true)
    private var items: List<ShelfItem> = emptyList()

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startInForeground()
        lifecycleScope.launch {
            placement = settingsPrefs.bubblePlacement.first()
            attachOverlay()
        }
        lifecycleScope.launch {
            repository.observeItems().collect { list ->
                items = list
                count.value = list.size
                refreshNotification()
            }
        }
        lifecycleScope.launch {
            combine(settingsPrefs.appearance, settingsPrefs.haptics) { appearance, hapticOn ->
                appearance to hapticOn
            }.collect { (appearance, hapticOn) ->
                haptics.value = hapticOn
                dark.value = appearance.isDark(systemDark())
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        startInForeground()
        when (intent?.action) {
            ACTION_HIDE -> {
                lifecycleScope.launch {
                    settingsPrefs.setBubbleEnabled(false)
                    stopSelf()
                }
            }
            ACTION_COLLAPSE -> {
                mode.value = BubbleMode.Collapsed
                dropHot.value = false
                relayout()
            }
        }
        if (!OverlayPermission.canDraw(this)) {
            stopSelf()
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        lifecycleScope.launch {
            val appearance = settingsPrefs.appearance.first()
            dark.value = appearance.isDark(systemDark())
        }
        mode.value = BubbleMode.Collapsed
        dropHot.value = false
        dragging = false
        relayout()
    }

    override fun onDestroy() {
        detachOverlay()
        super.onDestroy()
    }

    private fun startInForeground() {
        val notification = BubbleNotifications.build(this, count.value)
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(this, BubbleNotifications.NOTIFICATION_ID, notification, type)
    }

    private fun refreshNotification() {
        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(BubbleNotifications.NOTIFICATION_ID, BubbleNotifications.build(this, count.value))
    }

    @Synchronized
    private fun attachOverlay() {
        if (host != null) return
        if (!OverlayPermission.canDraw(this)) {
            stopSelf()
            return
        }
        val compose = OverlayComposeHost(this)
        val layout = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            overlayFlags(),
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.TOP or Gravity.LEFT
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
            val box = ScreenMetrics.box(windowManager, resources)
            val size = ScreenMetrics.dp(resources, BubbleChrome.HitDp)
            val (x, y) = BubblePosition.fromPlacement(placement, size, size, box)
            this.x = x
            this.y = y
        }
        compose.setOverlayContent {
            BubbleOverlayRoot(
                mode = mode,
                count = count,
                dark = dark,
                haptics = haptics,
                dropHot = dropHot,
                status = status,
                onTap = {
                    mode.value = if (mode.value == BubbleMode.Actions) {
                        BubbleMode.Collapsed
                    } else {
                        BubbleMode.Actions
                    }
                    relayout()
                },
                onDrag = { dx, dy -> moveBy(dx, dy) },
                onDragEnd = { snapAndSave() },
                onOpenShelf = { openShelf() },
                onShareAll = { shareAll() },
                onAskClear = {
                    mode.value = BubbleMode.ConfirmClear
                    relayout()
                },
                onConfirmClear = { clearShelf() },
                onCancelClear = {
                    mode.value = BubbleMode.Actions
                    relayout()
                },
                onHide = {
                    lifecycleScope.launch {
                        settingsPrefs.setBubbleEnabled(false)
                        stopSelf()
                    }
                },
            )
        }
        compose.setOnDragListener { _, event -> onDragEvent(event) }
        compose.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_OUTSIDE &&
                mode.value != BubbleMode.Collapsed &&
                mode.value != BubbleMode.DropTarget
            ) {
                mode.value = BubbleMode.Collapsed
                relayout()
            }
            false
        }
        try {
            windowManager.addView(compose, layout)
            host = compose
            params = layout
        } catch (_: Exception) {
            compose.release()
            stopSelf()
        }
    }

    private fun overlayFlags(): Int {
        var flags = (
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            flags = flags or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        }
        return flags
    }

    private fun onDragEvent(event: DragEvent): Boolean {
        when (event.action) {
            DragEvent.ACTION_DRAG_STARTED -> {
                if (!OverlayDrop.accepts(event.clipDescription)) return false
                mode.value = BubbleMode.DropTarget
                dropHot.value = false
                relayout()
                host?.let { lightHaptic(it, haptics.value) }
                return true
            }
            DragEvent.ACTION_DRAG_ENTERED -> {
                dropHot.value = true
                host?.let { lightHaptic(it, haptics.value) }
                return true
            }
            DragEvent.ACTION_DRAG_LOCATION -> return true
            DragEvent.ACTION_DRAG_EXITED -> {
                dropHot.value = false
                return true
            }
            DragEvent.ACTION_DROP -> {
                val payload = OverlayDrop.snapshot(event)
                val prefetched = prefetch(payload)
                dropHot.value = false
                mode.value = BubbleMode.Collapsed
                relayout()
                lifecycleScope.launch { commitPrefetch(prefetched, payload) }
                return true
            }
            DragEvent.ACTION_DRAG_ENDED -> {
                if (mode.value == BubbleMode.DropTarget) {
                    mode.value = BubbleMode.Collapsed
                    dropHot.value = false
                    relayout()
                }
                return true
            }
            else -> return false
        }
    }

    private sealed interface Prefetch {
        data class Files(val inbound: List<com.shobhankarthish.pocket.shelf.InboundFile>, val attempted: Int) : Prefetch
        data class Text(val text: String) : Prefetch
        data object None : Prefetch
    }

    private fun prefetch(payload: OverlayDropPayload): Prefetch {
        return when (val plan = OverlayDrop.plan(payload.uris, payload.texts)) {
            OverlayDropPlan.Empty -> Prefetch.None
            is OverlayDropPlan.Text -> Prefetch.Text(plan.text)
            is OverlayDropPlan.Streams -> {
                val inbound = plan.uris.mapNotNull { uri ->
                    runCatching { contentResolver.toInboundFile(uri) }.getOrNull()
                }
                Prefetch.Files(inbound, plan.uris.size)
            }
        }
    }

    private suspend fun commitPrefetch(prefetch: Prefetch, payload: OverlayDropPayload) {
        val message = when (prefetch) {
            Prefetch.None -> getString(R.string.bubble_drop_blocked)
            is Prefetch.Text -> {
                when (val result = ingestor.ingestText(prefetch.text)) {
                    is IngestResult.Ok -> getString(R.string.added)
                    IngestResult.Unsupported -> getString(R.string.unsupported)
                    IngestResult.Failed -> getString(R.string.ingest_failed)
                }
            }
            is Prefetch.Files -> {
                if (prefetch.inbound.isEmpty()) {
                    val textPlan = OverlayDrop.plan(emptyList(), payload.texts)
                    if (textPlan is OverlayDropPlan.Text) {
                        when (ingestor.ingestText(textPlan.text)) {
                            is IngestResult.Ok -> getString(R.string.added)
                            else -> getString(R.string.bubble_drop_blocked)
                        }
                    } else {
                        getString(R.string.bubble_drop_blocked)
                    }
                } else {
                    var tally = BatchTally(failed = prefetch.attempted - prefetch.inbound.size)
                    prefetch.inbound.forEach { inbound ->
                        tally = tally.plus(ingestor.ingest(inbound))
                    }
                    tallyMessage(tally)
                }
            }
        }
        showStatus(message)
        host?.let { lightHaptic(it, haptics.value) }
    }

    private fun tallyMessage(tally: BatchTally): String = when {
        tally.attempted <= 1 && tally.added == 1 -> getString(R.string.added)
        tally.attempted <= 1 && tally.unsupported == 1 -> getString(R.string.unsupported)
        tally.added == 0 && tally.unsupported == tally.attempted && tally.attempted > 0 ->
            getString(R.string.unsupported)
        tally.added == 0 -> getString(R.string.bubble_drop_blocked)
        tally.added == tally.attempted -> getString(R.string.added_some, tally.added, tally.attempted)
        else -> getString(R.string.added_some, tally.added, tally.attempted)
    }

    private fun showStatus(text: String) {
        status.value = text
        statusJob?.cancel()
        statusJob = lifecycleScope.launch {
            delay(2_200)
            if (status.value == text) status.value = null
            relayout()
        }
        relayout()
    }

    private fun moveBy(dx: Float, dy: Float) {
        val layout = params ?: return
        val view = host ?: return
        if (!dragging) {
            dragging = true
            dragX = layout.x
            dragY = layout.y
            mode.value = BubbleMode.Collapsed
        }
        dragX += dx.roundToInt()
        dragY += dy.roundToInt()
        val box = ScreenMetrics.box(windowManager, resources)
        val w = view.width.coerceAtLeast(ScreenMetrics.dp(resources, BubbleChrome.HitDp))
        val h = view.height.coerceAtLeast(ScreenMetrics.dp(resources, BubbleChrome.HitDp))
        val (x, y) = BubblePosition.clamp(dragX, dragY, w, h, box)
        layout.x = x
        layout.y = y
        runCatching { windowManager.updateViewLayout(view, layout) }
    }

    private fun snapAndSave() {
        val layout = params ?: return
        val view = host ?: return
        dragging = false
        val box = ScreenMetrics.box(windowManager, resources)
        val w = view.width.coerceAtLeast(ScreenMetrics.dp(resources, BubbleChrome.HitDp))
        val h = view.height.coerceAtLeast(ScreenMetrics.dp(resources, BubbleChrome.HitDp))
        val (snappedX, onLeft) = BubblePosition.snapX(layout.x, w, box)
        val (_, clampedY) = BubblePosition.clamp(snappedX, layout.y, w, h, box)
        layout.x = snappedX
        layout.y = clampedY
        runCatching { windowManager.updateViewLayout(view, layout) }
        placement = BubblePosition.toPlacement(snappedX, clampedY, w, h, box).copy(onLeft = onLeft)
        lifecycleScope.launch { settingsPrefs.setBubblePlacement(placement) }
    }

    private fun relayout() {
        val layout = params ?: return
        val view = host ?: return
        val box = ScreenMetrics.box(windowManager, resources)
        val current = mode.value
        val width = when (current) {
            BubbleMode.Collapsed -> {
                if (status.value != null) ScreenMetrics.dp(resources, 168)
                else ScreenMetrics.dp(resources, BubbleChrome.HitDp)
            }
            else -> ScreenMetrics.dp(resources, BubbleChrome.PanelWidthDp)
        }
        val height = when (current) {
            BubbleMode.DropTarget -> ScreenMetrics.dp(resources, BubbleChrome.DropHeightDp)
            else -> WindowManager.LayoutParams.WRAP_CONTENT
        }
        val estimateH = when (current) {
            BubbleMode.Collapsed -> ScreenMetrics.dp(resources, BubbleChrome.HitDp)
            BubbleMode.DropTarget -> ScreenMetrics.dp(resources, BubbleChrome.DropHeightDp)
            BubbleMode.Actions -> ScreenMetrics.dp(resources, 320)
            BubbleMode.ConfirmClear -> ScreenMetrics.dp(resources, 200)
        }
        val (x, y) = if (dragging && current == BubbleMode.Collapsed) {
            BubblePosition.clamp(dragX, dragY, width, estimateH, box)
        } else {
            BubblePosition.fromPlacement(placement, width, estimateH, box)
        }
        layout.x = x
        layout.y = y
        layout.width = width
        layout.height = height
        runCatching { windowManager.updateViewLayout(view, layout) }
        view.post {
            val laidOut = host ?: return@post
            val lp = params ?: return@post
            val liveBox = ScreenMetrics.box(windowManager, resources)
            val h = laidOut.height.coerceAtLeast(estimateH)
            val w = laidOut.width.coerceAtLeast(width)
            val (nx, ny) = BubblePosition.fromPlacement(placement, w, h, liveBox)
            if (lp.x != nx || lp.y != ny) {
                lp.x = nx
                lp.y = ny
                runCatching { windowManager.updateViewLayout(laidOut, lp) }
            }
        }
    }

    private fun openShelf() {
        mode.value = BubbleMode.Collapsed
        relayout()
        val launch = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(launch)
    }

    private fun shareAll() {
        val chosen = items
        if (chosen.isEmpty()) return
        val pairs = chosen.map { item -> item to repository.fileFor(item) }
        val prep = ShareOut.prepare(pairs)
        when (val decision = prep.decision) {
            ShareDecision.Nothing -> showStatus(getString(R.string.share_none))
            is ShareDecision.Choose -> openShelf()
            else -> {
                when (val outcome = ShareOut.execute(this, decision, pairs)) {
                    ShareOutcome.Nothing -> showStatus(getString(R.string.share_none))
                    is ShareOutcome.Partial -> showStatus(
                        getString(
                            R.string.share_partial,
                            outcome.shared,
                            outcome.shared + outcome.skipped,
                        ),
                    )
                    ShareOutcome.Done -> {
                        if (decision is ShareDecision.SendFiles && decision.mixedMimeWarning) {
                            showStatus(getString(R.string.share_mixed))
                        }
                    }
                }
                mode.value = BubbleMode.Collapsed
                relayout()
            }
        }
    }

    private fun clearShelf() {
        val targets = items
        lifecycleScope.launch {
            repository.remove(targets)
            mode.value = BubbleMode.Collapsed
            showStatus(getString(R.string.removed_some, targets.size))
        }
    }

    @Synchronized
    private fun detachOverlay() {
        val view = host ?: return
        runCatching { windowManager.removeView(view) }
        view.release()
        host = null
        params = null
    }

    private fun systemDark(): Boolean {
        val night = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return night == Configuration.UI_MODE_NIGHT_YES
    }

    companion object {
        const val ACTION_HIDE = "com.shobhankarthish.pocket.action.HIDE_BUBBLE"
        const val ACTION_COLLAPSE = "com.shobhankarthish.pocket.action.COLLAPSE_BUBBLE"

        fun start(context: Context) {
            val app = context.applicationContext
            if (!OverlayPermission.canDraw(app)) return
            ContextCompat.startForegroundService(app, Intent(app, BubbleService::class.java))
        }

        fun stop(context: Context) {
            context.applicationContext.stopService(
                Intent(context.applicationContext, BubbleService::class.java),
            )
        }

        suspend fun sync(context: Context) {
            val app = context.applicationContext as PocketApp
            val enabled = app.container.settingsPrefs.bubbleEnabled.first()
            if (enabled && OverlayPermission.canDraw(app)) {
                start(app)
            } else {
                stop(app)
            }
        }

        fun syncInBackground(context: Context, onDone: (() -> Unit)? = null) {
            val app = context.applicationContext
            CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                try {
                    sync(app)
                } finally {
                    onDone?.invoke()
                }
            }
        }
    }
}
