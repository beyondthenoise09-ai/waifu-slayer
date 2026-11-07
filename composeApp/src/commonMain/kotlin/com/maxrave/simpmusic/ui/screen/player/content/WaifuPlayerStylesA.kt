package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.viewModel.UIEvent

/* ─────────────────────────────────────────────────────────────────────────────
 * 1. WAIFU CLASSIC — circular artwork inside a violet progress ring, waveform seek bar,
 *    Queue / Lyrics pills. Near-black page with a faint violet glow at the top.
 * ───────────────────────────────────────────────────────────────────────────── */
@Composable
fun NowPlayingContentWaifuClassic(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(WaifuPalette.Bg2, WaifuPalette.Bg))),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(WaifuPalette.Accent.copy(alpha = 0.22f), Color.Transparent),
                            center = Offset(540f, 300f),
                            radius = 900f,
                        ),
                    ),
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
        ) {
            val ringSize = min(maxWidth * 0.80f, maxHeight * 0.40f)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WaifuTopBar(state, actions, title = "Now Playing", modifier = Modifier.padding(horizontal = 0.dp))
                Spacer(Modifier.weight(0.6f))
                WaifuProgressRing(
                    progress = state.sliderValue,
                    modifier = Modifier.size(ringSize),
                    strokeWidth = 4.dp,
                    startAngle = 0f,
                    sweepAngle = 360f,
                ) {
                    WaifuArtwork(
                        state = state,
                        actions = actions,
                        modifier = Modifier.size(ringSize - 28.dp),
                        shape = CircleShape,
                    )
                }
                Spacer(Modifier.weight(0.5f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WaifuTrackInfo(
                        state,
                        actions,
                        modifier = Modifier.weight(1f).padding(start = 48.dp),
                    )
                    WaifuLikeButton(state, actions)
                }
                Spacer(Modifier.height(16.dp))
                WaifuWaveSeekBar(
                    progress = state.sliderValue,
                    seed = state.screenData.nowPlayingTitle + state.screenData.artistName,
                    onChange = actions.onSliderChange,
                    onFinished = actions.onSliderChangeFinished,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                )
                WaifuTimeRow(state)
                Spacer(Modifier.height(12.dp))
                WaifuTransportRow(
                    state = state,
                    actions = actions,
                    playSize = 72.dp,
                    playBackground = WaifuPalette.Accent,
                )
                Spacer(Modifier.weight(0.4f))
                WaifuQueueLyricsRow(actions)
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/* ─────────────────────────────────────────────────────────────────────────────
 * 2. WAIFU AURA — large rounded-square artwork over a page that takes its colours from
 *    the artwork palette; thin seek bar, flat transport, volume slider.
 * ───────────────────────────────────────────────────────────────────────────── */
@Composable
fun NowPlayingContentWaifuAura(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val top = lerp(state.startColor.value, WaifuPalette.Bg, 0.45f)
    val mid = lerp(state.endColor.value, WaifuPalette.Bg, 0.7f)
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(top, mid, WaifuPalette.Bg))),
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
        ) {
            val artSize = min(maxWidth - 48.dp, maxHeight * 0.42f)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WaifuTopBar(state, actions, title = "", modifier = Modifier)
                Spacer(Modifier.weight(0.35f))
                WaifuArtwork(
                    state = state,
                    actions = actions,
                    modifier =
                        Modifier
                            .size(artSize)
                            .shadow(24.dp, RoundedCornerShape(20.dp), ambientColor = Color.Black, spotColor = Color.Black),
                    shape = RoundedCornerShape(20.dp),
                )
                Spacer(Modifier.weight(0.45f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WaifuTrackInfo(
                        state,
                        actions,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        artistColor = Color.White.copy(alpha = 0.7f),
                    )
                    WaifuLikeButton(state, actions, background = Color.White.copy(alpha = 0.12f))
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = state.sliderValue,
                    onValueChange = actions.onSliderChange,
                    onValueChangeFinished = actions.onSliderChangeFinished,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                        ),
                )
                WaifuTimeRow(state, color = Color.White.copy(alpha = 0.7f))
                Spacer(Modifier.height(8.dp))
                WaifuTransportRow(
                    state = state,
                    actions = actions,
                    playSize = 76.dp,
                    playBackground = Color.Transparent,
                    playTint = Color.White,
                    showShuffleRepeat = false,
                )
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = state.controllerState.volume,
                    onValueChange = { actions.onUIEvent(UIEvent.UpdateVolume(it)) },
                    colors =
                        SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = Color.White.copy(alpha = 0.85f),
                            inactiveTrackColor = Color.White.copy(alpha = 0.2f),
                        ),
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
                Spacer(Modifier.weight(0.2f))
                WaifuIconActionRow(state, actions, tint = Color.White)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

/* ─────────────────────────────────────────────────────────────────────────────
 * 3. WAIFU MINIMAL — an arch of artwork hanging from the top with the title laid over its
 *    foot, then almost nothing: a hairline seek bar and five quiet controls.
 * ───────────────────────────────────────────────────────────────────────────── */
@Composable
fun NowPlayingContentWaifuMinimal(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    Box(modifier = Modifier.fillMaxSize().background(WaifuPalette.Bg)) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val archHeight = maxHeight * 0.52f
            val archShape = RoundedCornerShape(topStart = 0.dp, topEnd = 0.dp, bottomStart = 160.dp, bottomEnd = 160.dp)
            Box(modifier = Modifier.fillMaxWidth().height(archHeight)) {
                WaifuArtwork(
                    state = state,
                    actions = actions,
                    modifier = Modifier.fillMaxSize(),
                    shape = archShape,
                )
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Black.copy(alpha = 0.35f), Color.Transparent, Color.Black.copy(alpha = 0.75f)),
                                ),
                                archShape,
                            ),
                )
                WaifuTrackInfo(
                    state,
                    actions,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(horizontal = 56.dp, vertical = 28.dp),
                )
            }
            Column(
                modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars),
            ) {
                WaifuTopBar(
                    state,
                    actions,
                    title = "",
                    buttonBackground = Color.Black.copy(alpha = 0.35f),
                )
                Spacer(Modifier.weight(1f))
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Slider(
                        value = state.sliderValue,
                        onValueChange = actions.onSliderChange,
                        onValueChangeFinished = actions.onSliderChangeFinished,
                        colors =
                            SliderDefaults.colors(
                                thumbColor = Color.White,
                                activeTrackColor = Color.White,
                                inactiveTrackColor = WaifuPalette.Border,
                            ),
                    )
                    WaifuTimeRow(state)
                    Spacer(Modifier.height(16.dp))
                    WaifuTransportRow(
                        state = state,
                        actions = actions,
                        playSize = 68.dp,
                        playBackground = Color.White,
                        playTint = Color.Black,
                        sideTint = Color.White,
                        activeTint = WaifuPalette.Accent2,
                    )
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        WaifuIconButton(SimpIcons.QueueMusic, "Queue", actions.onShowQueue, tint = WaifuPalette.TextSecondary)
                        WaifuLikeButton(state, actions, tint = WaifuPalette.TextSecondary)
                        WaifuIconButton(SimpIcons.Lyrics, "Lyrics", actions.onShowFullscreenLyrics, tint = WaifuPalette.TextSecondary)
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}
