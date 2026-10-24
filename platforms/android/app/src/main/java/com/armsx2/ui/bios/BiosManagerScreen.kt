package com.armsx2.ui.bios

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.armsx2.GameInfo
import com.armsx2.i18n.str
import com.armsx2.ui.common.ArmsBackdrop
import com.armsx2.ui.common.ArmsTopBar
import com.armsx2.ui.common.EmptyState
import com.armsx2.ui.common.RoundAction
import com.armsx2.ui.common.StatusChip
import com.armsx2.ui.settings.controllerFocusable
import com.armsx2.ui.theme.Success
import com.armsx2.nevoa.theme.NevoaPalette

@Composable
fun BiosManagerScreen(onBack: () -> Unit, game: GameInfo? = null, viewModel: BiosManagerViewModel = viewModel()) {
    val state = viewModel.state.value
    var deleteTarget by remember { mutableStateOf<InstalledBios?>(null) }
    var actionsMenuExpanded by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let(viewModel::import)
    }
    // Item 7: folder-import — point at a folder of BIOSes; all valid ones are imported and shown
    // in the list below to choose between (refresh parity).
    val folderPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let(viewModel::importFolder)
    }
    // Point the per-game controls at the long-pressed game (its settingsKey) when opened from
    // the library; null (drawer) falls back to the loaded game. setGameContext also refreshes.
    LaunchedEffect(game?.settingsKey) { viewModel.setGameContext(game?.settingsKey) }

    ArmsBackdrop {
        // Column + verticalScroll (not LazyColumn): controller nav registers rows via a SideEffect
        // when they compose, and LazyColumn never composes off-screen rows — so on a pad the
        // selection would stick partway down and the camera wouldn't follow. Composing every row
        // keeps registry nav + bringIntoView working. The BIOS list is short, so there's no cost.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArmsTopBar(
                title = str("setup.page.bios.title"),
                leading = {
                    Box(Modifier.controllerFocusable("bios.action.back", CircleShape, onConfirm = onBack)) {
                        RoundAction("←", str("action.back"), onBack)
                    }
                },
                actions = {
                    var actionsAnchor by remember {
                        mutableStateOf(androidx.compose.ui.geometry.Offset.Zero)
                    }
                    Box(
                        Modifier.controllerFocusable(
                            "bios.action.more", CircleShape,
                            onConfirm = { actionsMenuExpanded = true },
                        ).onGloballyPositioned {
                            // Bottom-left of the button, so the panel hangs below it as the
                            // dropdown it replaces did.
                            val p = it.positionInRoot()
                            actionsAnchor = androidx.compose.ui.geometry.Offset(
                                p.x, p.y + it.size.height,
                            )
                        },
                    ) {
                        RoundAction(
                            glyph = "⋮",
                            description = str("games.toolbar.more"),
                            onClick = { actionsMenuExpanded = true },
                            selected = actionsMenuExpanded,
                        )
                        BiosActionsMenu(
                            expanded = actionsMenuExpanded,
                            onDismiss = { actionsMenuExpanded = false },
                            onImportFile = { picker.launch(arrayOf("application/octet-stream", "*/*")) },
                            onImportFolder = { folderPicker.launch(null) },
                            onRefresh = viewModel::refresh,
                            anchor = actionsAnchor,
                        )
                    }
                },
                horizontalPadding = 0.dp,
            )
            if (state.items.isEmpty() && !state.busy) {
                EmptyState(
                    title = str("setup.bios.error.noneFound"),
                    message = str("setup.step.bios.description"),
                    actionLabel = str("action.import"),
                    onAction = { picker.launch(arrayOf("application/octet-stream", "*/*")) },
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                        .controllerFocusable(
                            "bios.empty.import",
                            RoundedCornerShape(24.dp),
                            onConfirm = { picker.launch(arrayOf("application/octet-stream", "*/*")) },
                        ),
                )
            } else {
                if (state.busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                state.items.forEach { item ->
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                        BiosRow(
                            item = item,
                            showGameAssign = state.gameKey != null,
                            perGameActive = state.perGameBios?.equals(item.file.name, ignoreCase = true) == true,
                            onSelect = { viewModel.select(item.file) },
                            onAssignGame = { viewModel.assignToGame(item) },
                            onClearGame = { viewModel.clearGameBios() },
                            onDelete = { deleteTarget = item },
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }

    deleteTarget?.let { item ->
        com.armsx2.ui.common.ConfirmOverlay(
            title = str("action.delete"),
            message = item.file.name,
            confirmLabel = str("action.delete"),
            destructive = true,
            idPrefix = "bios-delete",
            onConfirm = { viewModel.delete(item); deleteTarget = null },
            onDismiss = { deleteTarget = null },
        )
    }
    state.error?.let { error ->
        com.armsx2.ui.common.NotifyOverlay(
            title = str("setup.page.bios.title"),
            message = error,
            onDismiss = viewModel::dismissError,
            idPrefix = "bios.error",
        )
    }
}

@Composable
private fun BiosActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onImportFile: () -> Unit,
    onImportFolder: () -> Unit,
    onRefresh: () -> Unit,
    anchor: androidx.compose.ui.geometry.Offset,
) {
    fun closeThen(action: () -> Unit) {
        onDismiss()
        action()
    }

    if (!expanded) return
    // Anchored under its own ⋮ button — see the library's overflow menu for the reasoning.
    com.armsx2.ui.common.PadModal(
        key = "bios-actions",
        onDismiss = onDismiss,
        anchor = anchor,
        scrimAlpha = 0.32f,
    ) {
        Surface(
            modifier = Modifier.widthIn(min = 280.dp, max = 340.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.42f)),
        ) {
            Column {
                Text(
                    text = str("setup.page.bios.title"),
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                )
                BiosActionMenuItem("＋", str("action.import")) { closeThen(onImportFile) }
                BiosActionMenuItem("▣", str("action.importFolder")) { closeThen(onImportFolder) }
                BiosActionMenuItem("↻", str("games.card.refresh")) { closeThen(onRefresh) }
            }
        }
    }
}

@Composable
private fun BiosActionMenuItem(glyph: String, label: String, onClick: () -> Unit) {
    DropdownMenuItem(
        modifier = Modifier.controllerFocusable(
            controllerId = "bios-actions:$label",
            shape = RoundedCornerShape(12.dp),
            onConfirm = onClick,
        ),
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        onClick = onClick,
        leadingIcon = {
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(11.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = glyph,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        },
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
    )
}

@Composable
private fun BiosRow(
    item: InstalledBios,
    showGameAssign: Boolean,
    perGameActive: Boolean,
    onSelect: () -> Unit,
    onAssignGame: () -> Unit,
    onClearGame: () -> Unit,
    onDelete: () -> Unit,
) {
    var expanded by rememberSaveable(item.file.absolutePath) { mutableStateOf(false) }
    val toggle = { expanded = !expanded }
    Surface(
        modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NevoaPalette.Glass,
        border = BorderStroke(
            1.dp,
            if (item.selected || item.arcade) NevoaPalette.Selected.copy(alpha = 0.7f)
            else NevoaPalette.GlassBorder,
        ),
    ) {
        Column(Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .controllerFocusable(
                        "bios.details.${item.file.absolutePath}",
                        RoundedCornerShape(8.dp),
                        onConfirm = toggle,
                    )
                    .clickable(onClick = toggle)
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // An arcade board's BIOS shows the arcade mark (the ARMSX2 logo in red) in the flag's place.
                if (item.arcade) {
                    com.armsx2.ui.common.GlyphSizedImage(
                        item.info.regionFlag, 24.sp,
                        androidx.compose.ui.res.painterResource(com.armsx2.R.drawable.ic_arcade),
                    )
                } else {
                    Text(item.info.regionFlag, fontSize = 24.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    item.file.name,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.selected || item.arcade) StatusChip(str("backend.driver.active"), Success)
                if (perGameActive) {
                    Spacer(Modifier.width(6.dp))
                    StatusChip(str("bios.thisGame.active"), Success)
                }
                if (item.arcade) {
                    Spacer(Modifier.width(6.dp))
                    StatusChip(str("bios.arcade.badge"), MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Text(if (expanded) "−" else "+", color = NevoaPalette.TextDim)
            }
            if (expanded) {
                Text(
                    "${item.info.description} · ${item.info.versionString} · ${item.info.zone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = NevoaPalette.TextDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 36.dp, bottom = 6.dp),
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    // Per-game BIOS: pin this BIOS to the loaded game, or revert it to global.
                    if (showGameAssign && !item.arcade) {
                        if (perGameActive) {
                            TextButton(
                                onClick = onClearGame,
                                modifier = Modifier.controllerFocusable("bios.clearGame.${item.file.absolutePath}", onConfirm = onClearGame),
                            ) { Text(str("bios.useGlobal")) }
                        } else {
                            OutlinedButton(
                                onClick = onAssignGame,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.controllerFocusable("bios.thisGame.${item.file.absolutePath}", RoundedCornerShape(12.dp), onConfirm = onAssignGame),
                            ) { Text(str("bios.thisGame")) }
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    if (!item.selected && !item.arcade) OutlinedButton(
                        onClick = onSelect,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.controllerFocusable(
                            "bios.use.${item.file.absolutePath}",
                            RoundedCornerShape(12.dp),
                            onConfirm = onSelect,
                        ),
                    ) { Text(str("setup.bios.useSelected")) }
                    if (!item.selected) {
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = onDelete,
                            modifier = Modifier.controllerFocusable(
                                "bios.delete.${item.file.absolutePath}",
                                onConfirm = onDelete,
                            ),
                        ) { Text(str("action.delete")) }
                    }
                }
            }
        }
    }
}
