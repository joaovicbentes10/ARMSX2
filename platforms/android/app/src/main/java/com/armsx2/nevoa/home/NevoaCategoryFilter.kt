package com.armsx2.nevoa.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.armsx2.nevoa.theme.NevoaPalette
import com.armsx2.ui.settings.controllerFocusable

/** Shared, single-line category filters. The complete library supplies its own pad zone. */
@Composable
fun NevoaCategoryFilter(
    names: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    onManage: (String) -> Unit,
    modifier: Modifier = Modifier,
    controllerPrefix: String? = "nevoa.categories",
    focusedIndex: Int = -1,
) {
    val choices = listOf<String?>(null) + names
    Row(modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        choices.forEachIndexed { index, name ->
            val active = name == selected
            val shape = RoundedCornerShape(8.dp)
            val choose = { onSelect(name) }
            Text(
                name ?: "Todos os jogos",
                modifier = Modifier.widthIn(max = 240.dp)
                    .categoryFocus(focusedIndex == index)
                    .controllerFocusable(controllerPrefix?.let { "$it.$index" }, shape, onConfirm = choose)
                    .clip(shape)
                    .semantics { this.selected = active }
                    .background(if (active) NevoaPalette.SelectedSurface.copy(alpha = 0.38f) else NevoaPalette.Glass.copy(alpha = 0.03f))
                    .border(1.dp, if (focusedIndex == index) NevoaPalette.Selected else if (active) NevoaPalette.GlassBorder else androidx.compose.ui.graphics.Color.Transparent, shape)
                    .combinedClickable(onClick = choose, onLongClick = name?.let { { onManage(it) } })
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                color = if (active) NevoaPalette.Text else NevoaPalette.TextDim,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1, overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected != null) {
            androidx.compose.runtime.key("manage") {
                GlassButton("Gerenciar categoria", { onManage(selected) },
                    modifier = Modifier.categoryFocus(focusedIndex == choices.size).then(if (focusedIndex == choices.size) Modifier.border(1.dp, NevoaPalette.Selected, RoundedCornerShape(8.dp)) else Modifier),
                    quiet = true, controllerId = controllerPrefix?.let { "$it.manage" })
            }
        }
    }
}

@Composable
private fun Modifier.categoryFocus(focused: Boolean): Modifier {
    val requester = remember { BringIntoViewRequester() }
    LaunchedEffect(focused) { if (focused) requester.bringIntoView() }
    return bringIntoViewRequester(requester)
}
