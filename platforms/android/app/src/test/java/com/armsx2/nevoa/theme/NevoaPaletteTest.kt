package com.armsx2.nevoa.theme

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class NevoaPaletteTest {
    @Test fun text_on_background_is_AAA() = assertTrue(contrastRatio(NevoaPalette.Text, NevoaPalette.Background) >= 7.0)
    @Test fun dim_text_on_background_is_AA() = assertTrue(contrastRatio(NevoaPalette.TextDim, NevoaPalette.Background) >= 4.5)
    @Test fun selected_on_background_is_AA() = assertTrue(contrastRatio(NevoaPalette.Selected, NevoaPalette.Background) >= 4.5)
    @Test fun contrast_black_white_is_21() = assertEquals(21.0, contrastRatio(Color.Black, Color.White), 0.01)
    @Test fun scheme_background_matches_palette() = assertEquals(NevoaPalette.Background, NevoaPalette.scheme.background)
}
