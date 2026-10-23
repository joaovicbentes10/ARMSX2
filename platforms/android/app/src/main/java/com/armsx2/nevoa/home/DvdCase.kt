package com.armsx2.nevoa.home

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.armsx2.CustomCovers
import com.armsx2.GameInfo
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.common.GameCoverArt

private val CaseShape = RoundedCornerShape(6.dp)
private val CaseBand = Color(0xFF0B0F1C)

/** A PS2-style DVD case: dark top band, cover below. The selected case grows and glows. */
@Composable
fun DvdCase(game: GameInfo, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val height by animateDpAsState(if (selected) 176.dp else 128.dp, tween(180), label = "dvd-case")
    val context = LocalContext.current
    val hasCover = remember(game.uri, CustomCovers.version.value) {
        game.coverUrl != null || CustomCovers.fileFor(context, game) != null
    }
    Column(
        modifier
            .height(height)
            .aspectRatio(135f / 190f)
            .then(
                if (selected) {
                    // Stacked translucent rings rather than Modifier.shadow: tinted shadows render
                    // as opaque black boxes on some Adreno/Mali drivers (see controllerFocusable).
                    Modifier
                        .drawBehind {
                            for (i in 1..4) {
                                val g = (i * 3).dp.toPx()
                                drawRoundRect(
                                    color = NevoaPalette.Selected.copy(alpha = 0.10f),
                                    topLeft = Offset(-g, -g),
                                    size = Size(size.width + 2 * g, size.height + 2 * g),
                                    cornerRadius = CornerRadius(6.dp.toPx() + g),
                                )
                            }
                        }
                        .border(2.dp, NevoaPalette.Selected, CaseShape)
                } else {
                    Modifier.border(1.dp, NevoaPalette.GlassBorder, CaseShape)
                },
            )
            .clip(CaseShape)
            .background(NevoaPalette.Background)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier.fillMaxWidth().weight(0.09f).background(CaseBand),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                "PlayStation 2",
                color = NevoaPalette.Text,
                fontSize = 7.sp,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 6.dp),
            )
        }
        Box(Modifier.fillMaxWidth().weight(0.91f), contentAlignment = Alignment.Center) {
            if (hasCover) {
                GameCoverArt(game, Modifier.fillMaxSize())
            } else {
                Box(Modifier.fillMaxSize().background(NevoaPalette.Glass), contentAlignment = Alignment.Center) {
                    Text(
                        coverLabel(game.title, game.uri.lastPathSegment),
                        color = NevoaPalette.Text,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            }
        }
    }
}
