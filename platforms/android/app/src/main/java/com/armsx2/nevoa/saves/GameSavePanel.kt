package com.armsx2.nevoa.saves

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.armsx2.GameInfo
import com.armsx2.i18n.str
import com.armsx2.nevoa.home.GlassButton
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.common.PadModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

/** Shared by the selected cover, primary action and options. */
@Composable
fun rememberGameStates(game: GameInfo?): List<AssociatedState> {
    val context = LocalContext.current
    val runtimeState = com.armsx2.runtime.MainActivityRuntime.eState.value
    val nativeReady = com.armsx2.runtime.MainActivityRuntime.nativeReady.value
    val states by key(game?.uri) { produceState(emptyList<AssociatedState>(), GameStateCatalog.revision.intValue, runtimeState, nativeReady) {
        value = if (game != null && nativeReady && runtimeState == com.armsx2.EmuState.STOPPED)
            withContext(Dispatchers.IO) { GameStateCatalog.list(context, game) }
        else emptyList()
    } }
    return states
}

@Composable
fun GameSaveList(game: GameInfo, states: List<AssociatedState>, onContinue: (AssociatedState) -> Unit, onDismiss: () -> Unit) {
    PadModal(key = "nevoa.game-states", onDismiss = onDismiss, alignment = Alignment.Center) {
        Surface(Modifier.padding(16.dp).widthIn(max = 380.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            color = NevoaPalette.Background, contentColor = NevoaPalette.Text) {
            Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str("nevoa.saves.states"), style = MaterialTheme.typography.titleMedium)
                Text(game.title, color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodySmall)
                if (states.isEmpty()) Text(str("nevoa.saves.empty"), color = NevoaPalette.TextDim)
                states.forEach { state ->
                    val label = if (state.slot == -2) str("nevoa.saves.automatic") else "${str("nevoa.saves.slot")} ${state.slot + 1}"
                    GlassButton("$label · ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(state.modified))}",
                        { onContinue(state) }, quiet = true, controllerId = "nevoa.saved.${state.slot}")
                }
                GlassButton(str("action.close"), onDismiss, quiet = true, controllerId = "nevoa.saved.close")
            }
        }
    }
}
