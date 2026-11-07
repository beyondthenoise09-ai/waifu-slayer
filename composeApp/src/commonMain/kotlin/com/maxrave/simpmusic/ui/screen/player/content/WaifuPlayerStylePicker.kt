package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.maxrave.domain.manager.DataStoreManager

private data class StyleOption(
    val id: String,
    val name: String,
    val description: String,
)

private val waifuStyles =
    listOf(
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_WAIFU_CLASSIC, "WAIFU CLASSIC", "Circular artwork, violet ring, waveform seek bar"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_WAIFU_AURA, "WAIFU AURA", "Large artwork, colours from the cover, volume slider"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_WAIFU_MINIMAL, "WAIFU MINIMAL", "Arch artwork, hairline progress, quiet controls"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_WAIFU_IMMERSIVE, "WAIFU IMMERSIVE", "Full-screen artwork with a slow zoom"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_WAIFU_GLASS, "WAIFU GLASS", "Frosted cards and a floating control pill"),
    )

private val originalStyles =
    listOf(
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_SPOTIFY, "Spotify (original)", "Original SimpMusic layout, supports canvas and video"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_M3_EXPRESSIVE, "Material 3 Expressive (original)", "Original SimpMusic expressive layout"),
        StyleOption(DataStoreManager.NOW_PLAYING_STYLE_APPLE_MUSIC, "Apple Music (original)", "Original SimpMusic layout, needs Android 12+"),
    )

/** Plain-text name for the Settings row subtitle. */
fun waifuPlayerStyleName(id: String): String? = (waifuStyles + originalStyles).firstOrNull { it.id == id }?.name

/**
 * Style chooser with a small schematic preview of each layout. Picking one saves it at once
 * (DataStore) and the open player switches layout without touching playback.
 */
@Composable
fun WaifuPlayerStylePicker(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WaifuPalette.Surface,
        title = { Text("Player style", color = WaifuPalette.TextPrimary) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                waifuStyles.forEach { StyleRow(it, it.id == selected, onSelect) }
                Spacer(Modifier.height(12.dp))
                Text("Original styles", color = WaifuPalette.TextSecondary, style = MaterialTheme.typography.labelLarge)
                Spacer(Modifier.height(4.dp))
                originalStyles.forEach { StyleRow(it, it.id == selected, onSelect, showThumb = false) }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done", color = WaifuPalette.Accent2) }
        },
    )
}

@Composable
private fun StyleRow(
    option: StyleOption,
    isSelected: Boolean,
    onSelect: (String) -> Unit,
    showThumb: Boolean = true,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSelect(option.id) }
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showThumb) {
            StyleThumb(option.id)
            Spacer(Modifier.width(14.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(option.name, color = WaifuPalette.TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(option.description, color = WaifuPalette.TextSecondary, style = MaterialTheme.typography.bodySmall)
        }
        RadioButton(
            selected = isSelected,
            onClick = { onSelect(option.id) },
            colors =
                RadioButtonDefaults.colors(
                    selectedColor = WaifuPalette.Accent,
                    unselectedColor = WaifuPalette.TextSecondary,
                ),
        )
    }
}

/** 44×72 schematic of each layout. */
@Composable
private fun StyleThumb(id: String) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier =
            Modifier
                .size(width = 44.dp, height = 72.dp)
                .clip(shape)
                .background(WaifuPalette.Bg)
                .border(1.dp, WaifuPalette.Border, shape),
        contentAlignment = Alignment.TopCenter,
    ) {
        when (id) {
            DataStoreManager.NOW_PLAYING_STYLE_WAIFU_CLASSIC -> {
                Box(Modifier.padding(top = 12.dp).size(26.dp).clip(CircleShape).border(2.dp, WaifuPalette.Accent, CircleShape))
                Box(Modifier.padding(top = 50.dp).size(width = 28.dp, height = 3.dp).clip(CircleShape).background(WaifuPalette.Accent2))
            }
            DataStoreManager.NOW_PLAYING_STYLE_WAIFU_AURA -> {
                Box(Modifier.fillMaxWidth().height(72.dp).background(Brush.verticalGradient(listOf(WaifuPalette.Accent.copy(alpha = 0.55f), WaifuPalette.Bg))))
                Box(Modifier.padding(top = 10.dp).size(30.dp).clip(RoundedCornerShape(5.dp)).background(Color.White.copy(alpha = 0.85f)))
                Box(Modifier.padding(top = 54.dp).size(width = 28.dp, height = 2.dp).background(Color.White.copy(alpha = 0.7f)))
            }
            DataStoreManager.NOW_PLAYING_STYLE_WAIFU_MINIMAL -> {
                Box(
                    Modifier
                        .size(width = 44.dp, height = 36.dp)
                        .clip(RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp))
                        .background(WaifuPalette.Accent2.copy(alpha = 0.6f)),
                )
                Box(Modifier.padding(top = 56.dp).size(10.dp).clip(CircleShape).background(Color.White))
            }
            DataStoreManager.NOW_PLAYING_STYLE_WAIFU_IMMERSIVE -> {
                Box(Modifier.fillMaxWidth().height(72.dp).background(Brush.verticalGradient(listOf(WaifuPalette.Accent.copy(alpha = 0.8f), WaifuPalette.Accent2.copy(alpha = 0.3f), WaifuPalette.Bg))))
                Box(Modifier.padding(top = 52.dp).size(width = 26.dp, height = 3.dp).clip(CircleShape).background(Color.White))
            }
            DataStoreManager.NOW_PLAYING_STYLE_WAIFU_GLASS -> {
                Box(Modifier.padding(top = 4.dp).size(30.dp).clip(CircleShape).background(WaifuPalette.Accent.copy(alpha = 0.5f)))
                Box(
                    Modifier
                        .padding(top = 10.dp)
                        .size(width = 34.dp, height = 36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(8.dp)),
                )
                Box(
                    Modifier
                        .padding(top = 54.dp)
                        .size(width = 34.dp, height = 10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                )
            }
        }
    }
}
