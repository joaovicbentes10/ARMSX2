package com.armsx2.nevoa.home

import android.view.KeyEvent as AndroidKeyEvent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.nativeKeyCode
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.style.TextOverflow
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
import com.armsx2.ui.home.HomeSort
import com.armsx2.ui.home.LibraryKeyboard
import com.armsx2.ui.settings.SettingsControllerNav

private const val RowId = "nevoa.library"
private const val ConfigureId = "nevoa.configure"
private const val FullLibraryId = "nevoa.full-library"
private const val MenuId = "nevoa.menu"
private const val SearchId = "nevoa.search"
private const val SortId = "nevoa.sort"
private const val ClearSearchId = "nevoa.clear-search"

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
    // The ViewModel owns search and sorting. Adding recents here would put non-matching
    // games back into a search and override the user's chosen order.
    val games = state.visibleGames
    // While the first scan runs, show only the fog (it continues the intro's last frame) instead of
    // flashing the classic screen.
    val loading = !state.initialized && directories.isNotEmpty() && !classicRequested
    if (!loading && shouldShowClassic(state.initialized, state.scanning, games.size, directories.isNotEmpty(), classicRequested, filterActive = state.query.isNotEmpty())) {
        BackHandler(enabled = classicRequested) { classicRequested = false }
        HomeScreen(
            // The classic grid's B goes to the menu; when the user came from the row, it comes back here.
            onOpenMenu = if (classicRequested) ({ classicRequested = false }) else onOpenMenu,
            onOpenGameSettings = onOpenGameSettings,
            viewModel = viewModel,
        )
    } else {
        // One background for loading and the row, so its clock never restarts mid-way.
        Box(Modifier.fillMaxSize().background(NevoaPalette.Background)) {
            NevoaBackground(Modifier.fillMaxSize())
            if (!loading) {
                NevoaRow(
                    games = games,
                    query = state.query,
                    sort = state.sort,
                    onQuery = viewModel::setQuery,
                    onSort = viewModel::setSort,
                    onLaunch = { viewModel.launch(it) },
                    onOpenGameSettings = onOpenGameSettings,
                    onOpenClassic = { classicRequested = true },
                    onOpenMenu = onOpenMenu,
                )
            }
        }
    }
}

/** The PS5-style entrance plays once per process: after the intro, not on every return from a game. */
private object NevoaEntrance { var played = false }

/** Fades and slides an element in while [progress] goes from [start] to [end]. Read in the draw
 *  phase, so the entrance animates without recomposing the screen. */
private fun Modifier.enter(progress: () -> Float, start: Float, end: Float, dx: Float = 0f, dy: Float = 0f) =
    graphicsLayer {
        val p = ((progress() - start) / (end - start)).coerceIn(0f, 1f)
        val e = 1f - (1f - p) * (1f - p) * (1f - p)
        alpha = p
        translationX = (1f - e) * dx * density
        translationY = (1f - e) * dy * density
    }

@Composable
private fun NevoaRow(
    games: List<GameInfo>,
    query: String,
    sort: HomeSort,
    onQuery: (String) -> Unit,
    onSort: (HomeSort) -> Unit,
    onLaunch: (GameInfo) -> Unit,
    onOpenGameSettings: (GameInfo) -> Unit,
    onOpenClassic: () -> Unit,
    onOpenMenu: () -> Unit,
) {
    // Held by game, not position: launching a game moves it to the front of the recents.
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    val index = indexOfKey(games.map { it.uri.toString() }, selectedKey)
    val game = games.getOrNull(index)
    LaunchedEffect(game?.uri) { game?.let { selectedKey = it.uri.toString() } }
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
    val openSearch = { if (!busy) LibraryKeyboard.open(query, onQuery, NevoaStrings.SearchPlaceholder) }
    val clearSearch = { if (!busy) onQuery("") }
    val cycleSort = {
        if (!busy) onSort(HomeSort.entries[(sort.ordinal + 1) % HomeSort.entries.size])
    }
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
        if (!drawerOpen && game != null) {
            SettingsControllerNav.register(
                RowId,
                onConfirm = { onPlay(game) },
                onLeft = { move(-1) },
                onRight = { move(1) },
            )
        }
    }
    DisposableEffect(drawerOpen, game != null) { onDispose { SettingsControllerNav.unregister(RowId) } }
    LaunchedEffect(drawerOpen, game != null) {
        if (!drawerOpen) SettingsControllerNav.selectById(if (game != null) RowId else SearchId)
    }

    val listState = rememberLazyListState()
    LaunchedEffect(index, games.map { it.uri.toString() }) {
        if (games.isNotEmpty()) listState.animateScrollToItem((index - 1).coerceAtLeast(0))
    }

    // △ is not consumed by the registry path, so it reaches the focused Compose tree.
    val focus = remember { FocusRequester() }
    LaunchedEffect(drawerOpen, LibraryKeyboard.visible.value) {
        if (!drawerOpen && !LibraryKeyboard.visible.value) runCatching { focus.requestFocus() }
    }
    BackHandler(enabled = query.isNotEmpty() && !drawerOpen && !LibraryKeyboard.visible.value && !busy) {
        clearSearch()
    }

    val entrance = remember { Animatable(if (NevoaEntrance.played) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (!NevoaEntrance.played) {
            NevoaEntrance.played = true
            delay(250)
            entrance.animateTo(1f, tween(1100, easing = LinearEasing))
        }
    }
    val progress = { entrance.value }

    Box(
        Modifier
            .fillMaxSize()
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
        GlassButton(
            NevoaStrings.Menu,
            openMenu,
            Modifier.align(Alignment.TopStart).padding(16.dp).enter(progress, 0f, 0.45f, dy = -14f),
            quiet = true,
            controllerId = if (drawerOpen) null else MenuId,
        )
        Row(
            Modifier.align(Alignment.TopEnd).padding(16.dp).enter(progress, 0f, 0.45f, dy = -14f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            GlassButton(NevoaStrings.Search, openSearch, quiet = true, controllerId = if (drawerOpen) null else SearchId)
            GlassButton(
                "${NevoaStrings.Order}: ${when (sort) {
                    HomeSort.Title -> NevoaStrings.OrderTitle
                    HomeSort.RecentlyPlayed -> NevoaStrings.OrderRecent
                    HomeSort.Compatibility -> NevoaStrings.OrderCompatibility
                }}",
                cycleSort,
                quiet = true,
                controllerId = if (drawerOpen) null else SortId,
            )
            GlassButton(NevoaStrings.FullLibrary, openClassic, quiet = true, controllerId = if (drawerOpen) null else FullLibraryId)
        }
        if (query.isNotEmpty()) {
            Row(
                Modifier.align(Alignment.TopStart).padding(start = 48.dp, end = 48.dp, top = 64.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    "${NevoaStrings.Search}: $query",
                    modifier = Modifier.weight(1f, fill = false),
                    color = NevoaPalette.TextDim,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                GlassButton(NevoaStrings.ClearSearch, clearSearch, quiet = true, controllerId = if (drawerOpen) null else ClearSearchId)
            }
        }
        if (games.isEmpty()) {
            Column(
                Modifier.align(Alignment.Center).padding(48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(NevoaStrings.NoResults, color = NevoaPalette.Text, style = MaterialTheme.typography.titleLarge)
                Text(NevoaStrings.NoResultsHint, color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodyMedium)
            }
        }
        LazyRow(
            state = listState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .padding(top = if (query.isNotEmpty()) 64.dp else 0.dp)
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
                val caseStart = 0.12f + 0.07f * i.coerceAtMost(6)
                Row(
                    Modifier.enter(progress, caseStart, caseStart + 0.45f, dx = 70f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                ) {
                    DvdCase(
                        g,
                        selected = i == index,
                        onClick = {
                            if (busy) return@DvdCase
                            if (i == index) onPlay(g) else {
                                selectedKey = g.uri.toString()
                                MenuSfx.play(MenuSfx.Event.NAV)
                            }
                        },
                    )
                    if (i == index) {
                        GameDetailsPanel(
                            g,
                            modifier = Modifier.enter(progress, 0.35f, 0.8f, dx = 30f),
                            onPlay = { onPlay(g) },
                            onConfigure = { openSettings(g) },
                            configureControllerId = if (drawerOpen) null else ConfigureId,
                        )
                    }
                }
            }
        }
        LaunchBurst(active = pending != null) {
            pending?.let(onLaunch)
            pending = null
            launched = true
        }
    }
}
