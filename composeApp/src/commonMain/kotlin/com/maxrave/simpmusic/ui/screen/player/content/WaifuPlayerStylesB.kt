package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.maxrave.simpmusic.expect.ui.isLyricsBlurSupported
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.SimpIcons

/* ─────────────────────────────────────────────────────────────────────────────
 * 4. WAIFU IMMERSIVE — the artwork IS the page (full bleed, very slow zoom), darkened by a
 *    scrim so the controls stay readable. Left-aligned large title, slim violet seek bar.
 * ───────────────────────────────────────────────────────────────────────────── */
@Composable
fun NowPlayingContentWaifuImmersive(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val zoom by rememberInfiniteTransition(label = "waifuImmersiveZoom").animateFloat(
        initialValue = 1f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 20000, easing = LinearEasing), RepeatMode.Reverse),
        label = "zoom",
    )
    Box(modifier = Modifier.fillMaxSize().background(WaifuPalette.Bg)) {
        WaifuArtwork(
            state = state,
            actions = actions,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = zoom
                        scaleY = zoom
                    },
            shape = RectangleShape,
        )
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.55f),
                            0.28f to Color.Transparent,
                            0.50f to Color.Black.copy(alpha = 0.15f),
                            1f to WaifuPalette.Bg.copy(alpha = 0.97f),
                        ),
                    ),
        )
        Column(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
            WaifuTopBar(state, actions, title = "", buttonBackground = Color.Black.copy(alpha = 0.35f))
            Spacer(Modifier.weight(1f))
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WaifuTrackInfo(
                        state,
                        actions,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        artistColor = Color.White.copy(alpha = 0.78f),
                        large = true,
                    )
                    WaifuLikeButton(state, actions, background = Color.Black.copy(alpha = 0.35f))
                }
                Spacer(Modifier.height(8.dp))
                Slider(
                    value = state.sliderValue,
                    onValueChange = actions.onSliderChange,
                    onValueChangeFinished = actions.onSliderChangeFinished,
                    colors =
                        SliderDefaults.colors(
                            thumbColor = WaifuPalette.Accent2,
                            activeTrackColor = WaifuPalette.Accent,
                            inactiveTrackColor = Color.White.copy(alpha = 0.25f),
                        ),
                )
                WaifuTimeRow(state, color = Color.White.copy(alpha = 0.75f))
                Spacer(Modifier.height(8.dp))
                WaifuTransportRow(
                    state = state,
                    actions = actions,
                    playSize = 76.dp,
                    playBackground = Color.White,
                    playTint = Color.Black,
                    activeTint = WaifuPalette.Accent2,
                )
                Spacer(Modifier.height(20.dp))
                WaifuQueueLyricsRow(
                    actions = actions,
                    background = Color.Black.copy(alpha = 0.45f),
                    border = Color.White.copy(alpha = 0.18f),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

/* ─────────────────────────────────────────────────────────────────────────────
 * 5. WAIFU GLASS — layered translucent surfaces: a frosted card holding an open-arc
 *    artwork, and a separate floating control pill beneath it. Soft violet orbs behind.
 *    Blur is only used where the platform really blurs (Android 12+).
 * ───────────────────────────────────────────────────────────────────────────── */
@Composable
fun NowPlayingContentWaifuGlass(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
) {
    val glass = Color.White.copy(alpha = 0.08f)
    val glassEdge = Color.White.copy(alpha = 0.16f)
    Box(modifier = Modifier.fillMaxSize().background(WaifuPalette.Bg)) {
        if (isLyricsBlurSupported()) {
            WaifuArtwork(
                state = state,
                actions = actions,
                modifier = Modifier.fillMaxSize().blur(56.dp).alpha(0.28f),
                shape = RectangleShape,
            )
        }
        Box(
            modifier =
                Modifier
                    .size(340.dp)
                    .offset(x = (-110).dp, y = 80.dp)
                    .background(
                        Brush.radialGradient(listOf(WaifuPalette.Accent.copy(alpha = 0.40f), Color.Transparent)),
                        CircleShape,
                    ),
        )
        Box(
            modifier =
                Modifier
                    .size(300.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 90.dp, y = (-40).dp)
                    .background(
                        Brush.radialGradient(listOf(WaifuPalette.Accent2.copy(alpha = 0.28f), Color.Transparent)),
                        CircleShape,
                    ),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.systemBars)) {
            val ringSize = min(maxWidth * 0.62f, maxHeight * 0.30f)
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WaifuTopBar(state, actions, title = "Now Playing", buttonBackground = glass)
                Spacer(Modifier.height(8.dp))
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(36.dp))
                            .background(glass)
                            .border(1.dp, glassEdge, RoundedCornerShape(36.dp))
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    WaifuProgressRing(
                        progress = state.sliderValue,
                        modifier = Modifier.size(ringSize),
                        trackColor = Color.White.copy(alpha = 0.22f),
                        activeColor = WaifuPalette.Accent2,
                        strokeWidth = 4.dp,
                        startAngle = 135f,
                        sweepAngle = 270f,
                    ) {
                        WaifuArtwork(
                            state = state,
                            actions = actions,
                            modifier = Modifier.size(ringSize - 30.dp),
                            shape = CircleShape,
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    WaifuTrackInfo(state, actions)
                    Spacer(Modifier.height(12.dp))
                    WaifuWaveSeekBar(
                        progress = state.sliderValue,
                        seed = state.screenData.nowPlayingTitle + state.screenData.artistName,
                        onChange = actions.onSliderChange,
                        onFinished = actions.onSliderChangeFinished,
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        playedColor = Color.White,
                        restColor = Color.White.copy(alpha = 0.30f),
                        barCount = 44,
                    )
                    WaifuTimeRow(state, color = Color.White.copy(alpha = 0.7f))
                }
                Spacer(Modifier.weight(1f))
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(44.dp))
                            .background(glass)
                            .border(1.dp, glassEdge, RoundedCornerShape(44.dp))
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    WaifuTransportRow(
                        state = state,
                        actions = actions,
                        playSize = 64.dp,
                        playBackground = Color.White,
                        playTint = Color.Black,
                        activeTint = WaifuPalette.Accent2,
                    )
                }
                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    WaifuLikeButton(state, actions, background = glass)
                    WaifuIconButton(SimpIcons.QueueMusic, "Queue", actions.onShowQueue, background = glass)
                    WaifuIconButton(SimpIcons.Lyrics, "Lyrics", actions.onShowFullscreenLyrics, background = glass)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
