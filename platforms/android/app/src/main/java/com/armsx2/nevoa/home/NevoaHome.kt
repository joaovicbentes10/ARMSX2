package com.armsx2.nevoa.home

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import com.armsx2.EmuState
import com.armsx2.GameInfo
import com.armsx2.MenuSfx
import com.armsx2.navigation.UiNavigator
import com.armsx2.nevoa.NevoaStrings
import com.armsx2.nevoa.background.NevoaBackground
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.runtime.MainActivityRuntime
import com.armsx2.ui.home.HomeScreen
import com.armsx2.ui.home.HomeViewModel
import com.armsx2.ui.settings.SettingsControllerNav

private const val RowId = "nevoa.library"
private const val ConfigureId = "nevoa.configure"
private const val FullLibraryId = "nevoa.full-library"
private const val MenuId = "nevoa.menu"

/** PS5-style library: one row of cases over the fog. Falls back to the classic HomeScreen when
 *  there is nothing to show, or when the user asks for it (△ or the Full Library button). */
@Composable
fun NevoaHome(
    onOpenMenu: () -> Unit,
    onOpenGameSettings: (GameInfo) -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val state = viewModel.state.value
    val directories = MainActivityRuntime.romsDirs.value
    val nativeReady = MainActivityRuntime.nativeReady.value
    // Same loading HomeScreen does: the classic screen may never be composed.
    LaunchedEffect(directories, nativeReady) { viewModel.load(directories, nativeReady) }
    val refreshRequest = com.armsx2.data.library.LibraryRefresh.requests.intValue
    LaunchedEffect(refreshRequest, nativeReady, directories) {
        if (nativeReady) viewModel.onRefreshRequested(refreshRequest, directories)
    }
    var classicRequested by rememberSaveable { mutableStateOf(false) }
    val games = remember(state.recentGames, state.visibleGames) {
        libraryOrder(state.recentGames, state.visibleGames) { it.uri.toString() }
    }
    if (shouldShowClassic(state.initialized, state.scanning, games.size, directories.isNotEmpty(), classicRequested)) {
        BackHandler(enabled = classicRequested) { classicRequested = false }
        HomeScreen(
            // The classic grid's B goes to the menu; when the user came from the row, it comes back here.
            onOpenMenu = if (classicRequested) ({ classicRequested = false }) else onOpenMenu,
            onOpenGameSettings = onOpenGameSettings,
            viewModel = viewModel,
        )
    } else {
        NevoaRow(
            games = games,
            onLaunch = { viewModel.launch(it) },
            onOpenGameSettings = onOpenGameSettings,
            onOpenClassic = { classicRequested = true },
            onOpenMenu = onOpenMenu,
        )
    }
}

@Composable
private fun NevoaRow(
    games: List<GameInfo>,
    onLaunch: (GameInfo) -> Unit,
    onOpenGameSettings: (GameInfo) -> Unit,
    onOpenClassic: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    // Held by game, not position: launching a game moves it to the front of the recents.
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val index = indexOfKey(games.map { it.uri.toString() }, selectedKey)
    val game = games[index]
    // Every launch path goes through the gate, then the burst; the game starts when the burst ends.
    val gate = remember { LaunchGate() }
    var pending by remember { mutableStateOf<GameInfo?>(null) }
    var launched by remember { mutableStateOf(false) }
    fun onPlay(g: GameInfo) {
        if (gate.tryStart()) {
            MenuSfx.play(MenuSfx.Event.SELECT)
            pending = g
        }
    }
    // While the burst plays every action below is a no-op (touch, pad and Y alike).
    val busy = pending != null
    val openMenu = { if (!busy) onOpenMenu() }
    val openClassic = { if (!busy) onOpenClassic() }
    val openSettings = { g: GameInfo -> if (!busy) onOpenGameSettings(g) }
    // Reopen the gate only on a real signal: eState went non-STOPPED and came back, or the
    // launch never left STOPPED for a long time (failed). Ticker re-evaluates the timeout.
    LaunchedEffect(launched) {
        if (!launched) return@LaunchedEffect
        val start = System.currentTimeMillis()
        var sawNonStopped = false
        val ticker = flow { while (true) { emit(Unit); delay(500) } }
        snapshotFlow { MainActivityRuntime.eState.value }.combine(ticker) { s, _ -> s }.first { s ->
            if (s != EmuState.STOPPED) sawNonStopped = true
            shouldReopenGate(sawNonStopped, s == EmuState.STOPPED, System.currentTimeMillis() - start)
        }
        gate.reset()
        launched = false
    }
    // Coming back to the screen with the emulator stopped and no launch in flight.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && pending == null && !launched &&
                MainActivityRuntime.eState.value == EmuState.STOPPED
            ) gate.reset()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    fun move(delta: Int) {
        if (busy) return
        val next = moveSelection(index, delta, games.size)
        if (next != index) {
            selectedKey = games[next].uri.toString()
            MenuSfx.play(MenuSfx.Event.NAV)
        }
    }

    // The pad reaches Compose only through the SettingsControllerNav registry (see
    // MainActivityRuntime.dispatchKeyEvent). The row registers by hand rather than through
    // controllerFocusable, whose focus ring would outline the whole row.
    val drawerOpen = UiNavigator.drawerOpen.value
    SideEffect {
        if (!drawerOpen) {
            SettingsControllerNav.register(
                RowId,
                onConfirm = { onPlay(game) },
                onLeft = { move(-1) },
                onRight = { move(1) },
            )
        }
    }
    DisposableEffect(drawerOpen) { onDispose { SettingsControllerNav.unregister(RowId) } }
    LaunchedEffect(drawerOpen) { if (!drawerOpen) SettingsControllerNav.selectById(RowId) }

    val listState = rememberLazyListState()
    LaunchedEffect(index) { listState.animateScrollToItem((index - 1).coerceAtLeast(0)) }

    // △ is not consumed by the registry path, so it reaches the focused Compose tree.
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }

    Box(
        Modifier
            .fillMaxSize()
            .background(NevoaPalette.Background)
            .onPreviewKeyEvent { event ->
                if (event.type == KeyEventType.KeyDown && event.key.nativeKeyCode == AndroidKeyEvent.KEYCODE_BUTTON_Y) {
                    if (busy) return@onPreviewKeyEvent true
                    MenuSfx.play(MenuSfx.Event.SELECT)
                    openClassic()
                    true
                } else {
                    false
                }
            }
            .focusRequester(focus)
            .focusable(),
    ) {
        NevoaBackground(Modifier.fillMaxSize())
        GlassButton(
            NevoaStrings.Menu,
            openMenu,
            Modifier.align(Alignment.TopStart).padding(16.dp),
            controllerId = if (drawerOpen) null else MenuId,
        )
        GlassButton(
            NevoaStrings.FullLibrary,
            openClassic,
            Modifier.align(Alignment.TopEnd).padding(16.dp),
            controllerId = if (drawerOpen) null else FullLibraryId,
        )
        LazyRow(
            state = listState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(220.dp)
                .onGloballyPositioned {
                    val p = it.positionInRoot()
                    SettingsControllerNav.setPosition(RowId, p.x, p.y, it.size.width.toFloat())
                },
            contentPadding = PaddingValues(horizontal = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            itemsIndexed(games, key = { _, g -> g.uri.toString() }) { i, g ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    DvdCase(
                        g,
                        selected = i == index,
                        onClick = {
                            if (i == index) onPlay(g) else {
                                selectedKey = g.uri.toString()
                                MenuSfx.play(MenuSfx.Event.NAV)
                            }
                        },
                    )
                    if (i == index) {
                        GameDetailsPanel(
                            g,
                            onPlay = { onPlay(g) },
                            onConfigure = { openSettings(g) },
                            configureControllerId = if (drawerOpen) null else ConfigureId,
                        )
                    }
                }
            }
        }
        Text(
            NevoaStrings.PressToStart,
            color = NevoaPalette.TextDim,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        )
        LaunchBurst(active = pending != null) {
            pending?.let(onLaunch)
            pending = null
            launched = true
        }
    }
}
