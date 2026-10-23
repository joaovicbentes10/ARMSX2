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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.armsx2.GameInfo
import com.armsx2.PlayTime
import com.armsx2.nevoa.NevoaStrings
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.settings.controllerFocusable

private val ButtonShape = RoundedCornerShape(14.dp)

/** Frosted-glass button. [controllerId] registers it with the pad's nav registry. */
@Composable
internal fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    controllerId: String? = null,
) {
    Box(
        modifier
            .controllerFocusable(controllerId, ButtonShape, onConfirm = onClick)
            .clip(ButtonShape)
            .background(if (primary) NevoaPalette.Selected.copy(alpha = 0.22f) else NevoaPalette.Glass)
            .border(1.dp, if (primary) NevoaPalette.Selected else NevoaPalette.GlassBorder, ButtonShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Text(text, color = NevoaPalette.Text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Title, play history and the two actions for the selected game. */
@Composable
fun GameDetailsPanel(
    game: GameInfo,
    onPlay: () -> Unit,
    onConfigure: () -> Unit,
    modifier: Modifier = Modifier,
    configureControllerId: String? = null,
) {
    PlayTime.revision.value // re-read the totals after a session ends
    val last = PlayTime.formatLastPlayed(PlayTime.lastPlayedMillis(game.serial))
    val played = PlayTime.formatPlayed(PlayTime.playedSeconds(game.serial))
    Column(modifier.width(260.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            coverLabel(game.title, game.uri.lastPathSegment),
            color = NevoaPalette.Text,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            if (last.isEmpty()) NevoaStrings.NeverPlayed else "${NevoaStrings.Played}: $last",
            color = NevoaPalette.TextDim,
            style = MaterialTheme.typography.bodyMedium,
        )
        if (played.isNotEmpty()) {
            Text("${NevoaStrings.PlayTime}: $played", color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodyMedium)
        }
        Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassButton(NevoaStrings.Play, onPlay, primary = true)
            GlassButton(NevoaStrings.Configure, onConfigure, controllerId = configureControllerId)
        }
    }
}
