package com.armsx2.nevoa.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.armsx2.GameInfo
import com.armsx2.PlayTime
import com.armsx2.nevoa.NevoaStrings
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.settings.controllerFocusable

private val ButtonShape = RoundedCornerShape(8.dp)

/** Frosted-glass button. [controllerId] registers it with the pad's nav registry. */
@Composable
internal fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    quiet: Boolean = false,
    controllerId: String? = null,
) {
    Box(
        modifier
            .controllerFocusable(controllerId, ButtonShape, onConfirm = onClick)
            .clip(ButtonShape)
            .background(
                when {
                    primary -> NevoaPalette.Selected.copy(alpha = 0.22f)
                    quiet -> Color.Transparent
                    else -> NevoaPalette.Glass
                },
            )
            .then(
                if (quiet) Modifier else Modifier.border(
                    1.dp,
                    if (primary) NevoaPalette.Selected else NevoaPalette.GlassBorder,
                    ButtonShape,
                ),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = if (quiet) 12.dp else 20.dp, vertical = 10.dp),
    ) {
        Text(text, color = if (quiet) NevoaPalette.TextDim else NevoaPalette.Text, style = MaterialTheme.typography.labelLarge)
    }
}

/** A title, a short history line and one primary action. Secondary tools live in Options. */
@Composable
fun GameDetailsPanel(
    game: GameInfo,
    onPlay: () -> Unit,
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier,
    configureControllerId: String? = null,
    onCategories: (() -> Unit)? = null,
    states: List<com.armsx2.nevoa.saves.AssociatedState> = emptyList(),
    onContinue: (com.armsx2.nevoa.saves.AssociatedState) -> Unit = {},
    active: Boolean = true,
) {
    var options by androidx.compose.runtime.remember(game.uri) { androidx.compose.runtime.mutableStateOf(false) }
    var showStates by androidx.compose.runtime.remember(game.uri) { androidx.compose.runtime.mutableStateOf(false) }
    PlayTime.revision.value
    val last = PlayTime.lastPlayedMillis(game.serial)
    val played = PlayTime.formatPlayed(PlayTime.playedSeconds(game.serial))
    val history = if (last <= 0) "" else if (android.text.format.DateUtils.isToday(last))
        com.armsx2.i18n.I18n.get("nevoa.playedToday")
    else com.armsx2.i18n.I18n.get("nevoa.playedOn").format(java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT).format(java.util.Date(last)))
    val latest = states.firstOrNull()
    Column(modifier.width(280.dp).heightIn(max = 208.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            coverLabel(game.title, game.uri.lastPathSegment),
            color = NevoaPalette.Text,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (history.isNotEmpty() || played.isNotEmpty()) {
            Text(listOf(history, played).filter { it.isNotEmpty() }.joinToString(" · "),
                color = NevoaPalette.TextDim.copy(alpha = 0.8f), style = MaterialTheme.typography.bodySmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            GlassButton(if (latest == null) NevoaStrings.Play else com.armsx2.i18n.I18n.get("nevoa.saves.continue"),
                { if (active) { if (latest == null) onPlay() else onContinue(latest) } }, primary = true)
            val label = com.armsx2.i18n.I18n.get("nevoa.gameOptions")
            Box(Modifier.size(44.dp)
                .controllerFocusable(configureControllerId, androidx.compose.foundation.shape.CircleShape, onConfirm = { if (active) options = true })
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(NevoaPalette.Glass)
                .border(1.dp, NevoaPalette.GlassBorder, androidx.compose.foundation.shape.CircleShape)
                .semantics { contentDescription = label }
                .clickable { if (active) options = true }
                .drawBehind {
                    for (i in -1..1) drawCircle(NevoaPalette.TextDim, 1.5.dp.toPx(), androidx.compose.ui.geometry.Offset(size.width / 2 + i * 6.dp.toPx(), size.height / 2))
                })
        }
    }
    if (options && active) com.armsx2.ui.common.PadModal(key = "nevoa.game-options", onDismiss = { options = false }) {
        androidx.compose.material3.Surface(Modifier.padding(20.dp).widthIn(max = 320.dp).fillMaxWidth(),
            shape = RoundedCornerShape(14.dp), color = NevoaPalette.Background, contentColor = NevoaPalette.Text) {
            Column(Modifier.heightIn(max = 300.dp).verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(com.armsx2.i18n.I18n.get("nevoa.gameOptions"), style = MaterialTheme.typography.titleMedium)
                Text(game.title, color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                GlassButton(NevoaStrings.Play, { options = false; onPlay() }, Modifier.fillMaxWidth().padding(top = 10.dp), quiet = true, controllerId = "nevoa.options.play")
                GlassButton("${com.armsx2.i18n.I18n.get("nevoa.saves.states")} (${states.size})", { options = false; showStates = true }, Modifier.fillMaxWidth(), quiet = true, controllerId = "nevoa.options.states")
                GlassButton(NevoaStrings.Configure, { options = false; onConfigure() }, Modifier.fillMaxWidth(), quiet = true, controllerId = "nevoa.options.configure")
                onCategories?.let { action ->
                    GlassButton(com.armsx2.i18n.I18n.get("nevoa.gameCategories"), { options = false; action() }, Modifier.fillMaxWidth(), quiet = true, controllerId = "nevoa.options.categories")
                }
            }
        }
    }
    if (showStates && active) com.armsx2.nevoa.saves.GameSaveList(game, states,
        onContinue = { showStates = false; onContinue(it) }, onDismiss = { showStates = false })
}
