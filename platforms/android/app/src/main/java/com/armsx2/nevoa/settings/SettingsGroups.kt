package com.armsx2.nevoa.settings

import com.armsx2.navigation.SettingsCategory

/** Presentation groups only. The existing categories remain the source of settings behavior. */
enum class SettingsGroup(val label: String, val glyph: String) {
    System("Sistema", "⌂"),
    Media("Imagem e som", "◫"),
    Input("Controle", "⌁"),
    Extras("Conexões e extras", "◎"),
}

fun groupFor(category: SettingsCategory): SettingsGroup = when (category) {
    SettingsCategory.General,
    SettingsCategory.Info,
    SettingsCategory.Performance,
    SettingsCategory.Advanced,
    SettingsCategory.About -> SettingsGroup.System

    SettingsCategory.Graphics,
    SettingsCategory.Audio -> SettingsGroup.Media

    SettingsCategory.Controls,
    SettingsCategory.Hotkeys,
    SettingsCategory.Skins,
    SettingsCategory.OnScreen -> SettingsGroup.Input

    SettingsCategory.Network,
    SettingsCategory.Patches -> SettingsGroup.Extras
}
