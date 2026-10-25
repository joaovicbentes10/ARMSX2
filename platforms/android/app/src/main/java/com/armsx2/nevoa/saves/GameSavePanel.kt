package com.armsx2.nevoa.saves

import android.widget.Toast
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
import com.armsx2.i18n.I18n
import com.armsx2.nevoa.home.GlassButton
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.common.PadModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

@Composable
fun GameSavePanel(game: GameInfo, onContinue: (AssociatedState) -> Unit, controllerId: String?) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val runtimeState = com.armsx2.runtime.MainActivityRuntime.eState.value
    val nativeReady = com.armsx2.runtime.MainActivityRuntime.nativeReady.value
    val states by produceState(emptyList<AssociatedState>(), game.uri, GameStateCatalog.revision.intValue, runtimeState, nativeReady) {
        value = if (nativeReady && runtimeState == com.armsx2.EmuState.STOPPED)
            withContext(Dispatchers.IO) { GameStateCatalog.list(context, game) }
        else emptyList()
    }
    var open by remember(game.uri) { mutableStateOf(false) }
    var checking by remember(game.uri) { mutableStateOf(false) }
    fun resume(state: AssociatedState) {
        if (checking) return
        checking = true
        scope.launch {
            val current = withContext(Dispatchers.IO) { StateAssociation.read(state.file, state.identity) }
            checking = false
            if (current == null) Toast.makeText(context, I18n.get("nevoa.saves.changed"), Toast.LENGTH_LONG).show()
            else { open = false; onContinue(current) }
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        GlassButton("${str("nevoa.saves.states")} (${states.size})", { open = true }, quiet = true, controllerId = controllerId?.let { "$it.list" })
        states.firstOrNull()?.let { state ->
            GlassButton(str("nevoa.saves.continue"), { resume(state) }, quiet = true, controllerId = controllerId?.let { "$it.continue" })
        }
    }
    if (open) PadModal(key = "nevoa.game-states", onDismiss = { if (!checking) open = false }, alignment = Alignment.Center) {
        Surface(Modifier.padding(16.dp).widthIn(max = 420.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp),
            color = NevoaPalette.Background, contentColor = NevoaPalette.Text) {
            Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(str("nevoa.saves.states"), style = MaterialTheme.typography.titleMedium)
                Text(game.title, color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodySmall)
                if (states.isEmpty()) Text(str("nevoa.saves.empty"), color = NevoaPalette.TextDim)
                states.forEach { state ->
                    val label = if (state.slot == -2) str("nevoa.saves.automatic") else "${str("nevoa.saves.slot")} ${state.slot + 1}"
                    GlassButton("$label · ${DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(state.modified))}",
                        { resume(state) }, quiet = true, controllerId = "nevoa.saved.${state.slot}")
                }
                GlassButton(str("action.close"), { if (!checking) open = false }, quiet = true, controllerId = "nevoa.saved.close")
            }
        }
    }
}
