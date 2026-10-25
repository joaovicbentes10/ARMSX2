package com.armsx2.nevoa.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.settings.controllerFocusable
import compose.icons.LineAwesomeIcons
import compose.icons.lineawesomeicons.AngleRightSolid

/** Compact disclosure shared by pause panes and state management. */
@Composable
fun NevoaSection(
    title: String,
    controllerId: String = "pause.section.$title",
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable(title) { mutableStateOf(false) }
    val angle by animateFloatAsState(if (expanded) 90f else 0f, tween(160), label = "nevoa-section")
    val toggle = { expanded = !expanded }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Surface(
            onClick = toggle,
            modifier = Modifier.fillMaxWidth().controllerFocusable(controllerId, RoundedCornerShape(8.dp), onConfirm = toggle),
            shape = RoundedCornerShape(8.dp),
            color = Color.Transparent,
            contentColor = NevoaPalette.Text,
        ) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(title, Modifier.weight(1f), color = NevoaPalette.TextDim, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Icon(LineAwesomeIcons.AngleRightSolid, contentDescription = null, tint = NevoaPalette.TextDim,
                    modifier = Modifier.size(15.dp).graphicsLayer { rotationZ = angle })
            }
        }
        AnimatedVisibility(expanded, enter = fadeIn(tween(150)) + expandVertically(tween(180)), exit = fadeOut(tween(100)) + shrinkVertically(tween(160))) {
            Column(Modifier.padding(horizontal = 6.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp), content = content)
        }
    }
}
