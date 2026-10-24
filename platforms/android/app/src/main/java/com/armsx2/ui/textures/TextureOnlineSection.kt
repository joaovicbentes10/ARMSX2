package com.armsx2.ui.textures

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.armsx2.TextureCatalog
import com.armsx2.TexturePackLinks
import com.armsx2.i18n.str
import com.armsx2.ui.common.GlassPanel
import com.armsx2.ui.settings.SettingsControllerNav
import com.armsx2.ui.settings.controllerFocusable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

/**
 * Compact entry to the catalog and the current download. Browse opens [TexturePackBrowser].
 * The catalog loads here so the browser can open immediately.
 *
 * The whole catalog is browsable with no game running. Each pack names the serials it belongs to, so
 * the install target comes from the pack itself rather than from whatever happens to be loaded; for a
 * multi-region pack it is the serial of the copy the player has ([TextureCatalog.Pack.installSerialFor]).
 * New packs reach us over Discord and are uploaded by hand, so that is where the submit link goes.
 */
@Composable
fun TextureOnlineSection(
    /** Serial of the game in context, if any. The browser opens on its packs, and a pack covering it
     *  installs there. */
    serial: String?,
    /** Serials present in the user's library, for the browser's My games. */
    librarySerials: Set<String> = emptySet(),
    /** Serials of games the player can boot (the library scan and the game in context), upper-case.
     *  Picks a multi-region pack's install folder. Unlike [librarySerials] it leaves out installed
     *  pack folders, so a pack installed under the wrong region's serial cannot vouch for itself. */
    ownedSerials: Set<String> = emptySet(),
    modifier: Modifier = Modifier,
    onInstalled: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    var fromCache by remember { mutableStateOf(false) }
    var packs by remember { mutableStateOf<List<TextureCatalog.Pack>>(emptyList()) }
    var links by remember { mutableStateOf<Map<String, TexturePackLinks.Links>>(emptyMap()) }
    var browserOpen by rememberSaveable { mutableStateOf(false) }
    var fetchVersion by remember { mutableIntStateOf(0) }

    LaunchedEffect(fetchVersion) {
        loading = true
        // Two files on the same server, asked for side by side.
        val (result, packLinks) = withContext(Dispatchers.IO) {
            val packLinks = async { TexturePackLinks.fetch(context) }
            TextureCatalog.fetch(context) to packLinks.await()
        }
        links = packLinks
        packs = result?.packs.orEmpty()
        fromCache = result?.fromCache == true
        failed = result == null
        loading = false
    }

    // A pack that finished installing, with the browser open or after it closed, refreshes the
    // installed packs on the texture screen.
    val installedCount = TexturePackDownloads.installedCount.intValue
    var seen by remember { mutableIntStateOf(installedCount) }
    LaunchedEffect(installedCount) {
        if (installedCount != seen) {
            seen = installedCount
            onInstalled()
        }
    }

    val open = { if (packs.isNotEmpty()) browserOpen = true }
    // A touch opens it with nothing selected: closing it then hands the controller back nothing,
    // rather than a selection left somewhere down the screen that the page would scroll to.
    val openByTouch = {
        SettingsControllerNav.clearSelection()
        open()
    }
    val browseOrRetry: () -> Unit = {
        if (failed) fetchVersion++ else openByTouch()
    }
    val browseOrRetryFromPad: () -> Unit = {
        if (failed) fetchVersion++ else open()
    }
    GlassPanel(modifier) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(str("textures.online.title"), style = MaterialTheme.typography.titleMedium)
                    when {
                        loading -> Text(str("textures.online.loading"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        failed -> Text(str("textures.online.failed"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                        else -> Text(str("textures.online.count").format(packs.size), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                TextButton(
                    onClick = browseOrRetry,
                    enabled = failed || packs.isNotEmpty(),
                    modifier = Modifier.controllerFocusable("textures.online.browse", onConfirm = browseOrRetryFromPad),
                ) { Text(str(if (failed) "games.card.refresh" else "textures.browser.open")) }
            }
            DownloadStatus(Modifier.fillMaxWidth().padding(top = 4.dp), dark = false)
            // New packs come to us over Discord and are uploaded to the catalog by hand; there is no
            // public repo to send people to, so the link is the server itself.
            val contact = { uriHandler.openUri(com.armsx2.navigation.DiscordUrl) }
            TextButton(
                onClick = contact,
                modifier = Modifier.controllerFocusable("tex.contribute", onConfirm = contact),
            ) { Text(str("textures.online.submitDiscord"), style = MaterialTheme.typography.bodySmall) }
            if (fromCache) {
                Spacer(Modifier.height(6.dp))
                Text(
                    str("textures.online.cached"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }

    if (browserOpen && packs.isNotEmpty()) {
        TexturePackBrowser(
            packs = packs,
            links = links,
            contextSerial = serial,
            librarySerials = librarySerials,
            ownedSerials = ownedSerials,
            onClose = { browserOpen = false },
        )
    }
}
