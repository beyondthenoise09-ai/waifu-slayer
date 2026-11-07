package com.maxrave.simpmusic.ui.screen.player.content

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.FavoriteBorder
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.QueueMusic
import com.maxrave.simpmusic.ui.icon.Repeat
import com.maxrave.simpmusic.ui.icon.RepeatOne
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.SkipNext
import com.maxrave.simpmusic.ui.icon.SkipPrevious
import com.maxrave.simpmusic.viewModel.UIEvent

/**
 * WAIFU SLAYER player building blocks.
 *
 * Every WAIFU style renders from the SAME [NowPlayingContentState] and calls the SAME
 * [NowPlayingContentActions] as the three original styles, so there is exactly one playback
 * engine, one queue and one set of sheets. Styles only differ in layout and look.
 */
internal object WaifuPalette {
    val Bg = Color(0xFF08050F)
    val Bg2 = Color(0xFF100B19)
    val Surface = Color(0xFF17111F)
    val Accent = Color(0xFF9B4DFF)
    val Accent2 = Color(0xFFC084FC)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFA7A0B2)
    val Border = Color(0xFF30263A)
}

internal fun formatPlayerTime(ms: Long): String {
    val total = (ms.coerceAtLeast(0L) / 1000L).toInt()
    val m = total / 60
    val s = total % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

/** Elapsed / remaining labels for the current timeline. Live streams show "LIVE". */
internal fun NowPlayingContentState.elapsedText(): String = formatPlayerTime(timelineState.current)

internal fun NowPlayingContentState.totalText(): String = if (timelineState.isLive) "LIVE" else formatPlayerTime(timelineState.total)

/** Deterministic bar heights (0.25..1.0). A visual seek bar only — NOT real audio analysis. */
private fun waveHeights(
    seed: String,
    count: Int,
): FloatArray {
    var x = seed.hashCode().toLong() * 6364136223846793005L + 1442695040888963407L
    val raw = FloatArray(count)
    for (i in 0 until count) {
        x = x * 6364136223846793005L + 1442695040888963407L
        raw[i] = ((x ushr 33) % 1000L) / 1000f
    }
    val out = FloatArray(count)
    for (i in 0 until count) {
        val prev = raw[(i - 1).coerceAtLeast(0)]
        val next = raw[(i + 1).coerceAtMost(count - 1)]
        out[i] = (0.25f + 0.75f * ((prev + raw[i] * 2f + next) / 4f)).coerceIn(0.25f, 1f)
    }
    return out
}

/** Interactive waveform-style seek bar. Tap or drag to seek. */
@Composable
internal fun WaifuWaveSeekBar(
    progress: Float,
    seed: String,
    onChange: (Float) -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    playedColor: Color = WaifuPalette.Accent,
    restColor: Color = WaifuPalette.TextSecondary.copy(alpha = 0.45f),
    barCount: Int = 52,
) {
    val heights = remember(seed, barCount) { waveHeights(seed, barCount) }
    val currentOnChange = rememberUpdatedState(onChange)
    val currentOnFinished = rememberUpdatedState(onFinished)
    Canvas(
        modifier =
            modifier
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        currentOnChange.value((offset.x / size.width).coerceIn(0f, 1f))
                        currentOnFinished.value()
                    }
                }.pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = { currentOnFinished.value() },
                        onDragCancel = { currentOnFinished.value() },
                    ) { change, _ ->
                        change.consume()
                        currentOnChange.value((change.position.x / size.width).coerceIn(0f, 1f))
                    }
                },
    ) {
        val barW = size.width / (barCount * 2f - 1f)
        val p = progress.coerceIn(0f, 1f)
        for (i in 0 until barCount) {
            val h = heights[i] * size.height
            val top = (size.height - h) / 2f
            val played = (i + 0.5f) / barCount <= p
            drawRoundRect(
                color = if (played) playedColor else restColor,
                topLeft = Offset(i * barW * 2f, top),
                size = Size(barW, h),
                cornerRadius = CornerRadius(barW / 2f, barW / 2f),
            )
        }
    }
}

/** Arc progress ring that wraps [content] (usually the artwork). Display only. */
@Composable
internal fun WaifuProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    trackColor: Color = WaifuPalette.Border,
    activeColor: Color = WaifuPalette.Accent,
    strokeWidth: Dp = 4.dp,
    startAngle: Float = 135f,
    sweepAngle: Float = 270f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier =
            modifier.drawBehind {
                val sw = strokeWidth.toPx()
                val arcSize = Size(size.width - sw, size.height - sw)
                val topLeft = Offset(sw / 2f, sw / 2f)
                drawArc(
                    color = trackColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
                drawArc(
                    color = activeColor,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle * progress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                )
            },
        contentAlignment = Alignment.Center,
        content = content,
    )
}

/** Album artwork. Also feeds the bitmap to the shell so palette colours keep working. */
@Composable
internal fun WaifuArtwork(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
) {
    val url = state.screenData.thumbnailURL
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalPlatformContext.current)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .diskCacheKey(url + "BIGGER")
                .crossfade(true)
                .build(),
        placeholder = rememberHolderPainter(square = true),
        error = rememberHolderPainter(square = true),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        onSuccess = { actions.onArtworkBitmap(it.result.image.toImageBitmap()) },
        modifier = modifier.clip(shape),
    )
}

@Composable
internal fun WaifuIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    buttonSize: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    background: Color = Color.Transparent,
) {
    Box(
        modifier =
            modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
internal fun WaifuTopBar(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    title: String,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    buttonBackground: Color = Color.Transparent,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        WaifuIconButton(
            icon = state.dismissIcon,
            contentDescription = "Close player",
            onClick = actions.onDismiss,
            tint = tint,
            background = buttonBackground,
        )
        Text(
            text = title,
            color = tint,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 8.dp),
            textAlign = TextAlign.Center,
        )
        WaifuIconButton(
            icon = SimpIcons.MoreVert,
            contentDescription = "More",
            onClick = actions.onShowMoreSheet,
            tint = tint,
            background = buttonBackground,
        )
    }
}

@Composable
internal fun WaifuLikeButton(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    likedTint: Color = WaifuPalette.Accent2,
    background: Color = Color.Transparent,
) {
    val liked = state.controllerState.isLiked
    WaifuIconButton(
        icon = if (liked) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
        contentDescription = if (liked) "Remove from liked songs" else "Like",
        onClick = { actions.onUIEvent(UIEvent.ToggleLike) },
        modifier = modifier,
        tint = if (liked) likedTint else tint,
        background = background,
    )
}

@Composable
internal fun WaifuTrackInfo(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Center,
    titleColor: Color = WaifuPalette.TextPrimary,
    artistColor: Color = WaifuPalette.TextSecondary,
    titleWeight: FontWeight = FontWeight.Bold,
    large: Boolean = false,
) {
    androidx.compose.foundation.layout.Column(modifier = modifier) {
        Text(
            text = state.screenData.nowPlayingTitle,
            color = titleColor,
            style = if (large) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge,
            fontWeight = titleWeight,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = state.screenData.artistName,
            color = artistColor,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = textAlign,
            modifier = Modifier.fillMaxWidth().clickable(onClick = actions.onNavigateToArtist),
        )
    }
}

@Composable
internal fun WaifuTimeRow(
    state: NowPlayingContentState,
    modifier: Modifier = Modifier,
    color: Color = WaifuPalette.TextSecondary,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(state.elapsedText(), color = color, style = MaterialTheme.typography.labelMedium)
        Text(state.totalText(), color = color, style = MaterialTheme.typography.labelMedium)
    }
}

/** Shuffle · previous · play/pause · next · repeat. */
@Composable
internal fun WaifuTransportRow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    playSize: Dp = 72.dp,
    playBackground: Color = WaifuPalette.Accent,
    playTint: Color = Color.White,
    playShape: Shape = CircleShape,
    sideTint: Color = Color.White,
    activeTint: Color = WaifuPalette.Accent2,
    showShuffleRepeat: Boolean = true,
) {
    val control = state.controllerState
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        if (showShuffleRepeat) {
            WaifuIconButton(
                icon = SimpIcons.Shuffle,
                contentDescription = "Shuffle",
                onClick = { actions.onUIEvent(UIEvent.Shuffle) },
                tint = if (control.isShuffle) activeTint else sideTint.copy(alpha = 0.7f),
            )
        }
        WaifuIconButton(
            icon = SimpIcons.SkipPrevious,
            contentDescription = "Previous",
            onClick = { actions.onUIEvent(UIEvent.Previous) },
            tint = sideTint,
            buttonSize = 52.dp,
            iconSize = 32.dp,
        )
        Box(
            modifier =
                Modifier
                    .size(playSize)
                    .clip(playShape)
                    .background(playBackground)
                    .clickable { actions.onUIEvent(UIEvent.PlayPause) },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = if (control.isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
                contentDescription = if (control.isPlaying) "Pause" else "Play",
                tint = playTint,
                modifier = Modifier.size(playSize * 0.5f),
            )
        }
        WaifuIconButton(
            icon = SimpIcons.SkipNext,
            contentDescription = "Next",
            onClick = { actions.onUIEvent(UIEvent.Next) },
            tint = sideTint,
            buttonSize = 52.dp,
            iconSize = 32.dp,
        )
        if (showShuffleRepeat) {
            val repeatOn = control.repeatState !is RepeatState.None
            WaifuIconButton(
                icon = if (control.repeatState is RepeatState.One) SimpIcons.RepeatOne else SimpIcons.Repeat,
                contentDescription = "Repeat",
                onClick = { actions.onUIEvent(UIEvent.Repeat) },
                tint = if (repeatOn) activeTint else sideTint.copy(alpha = 0.7f),
            )
        }
    }
}

/** Pill-shaped labelled button used for Queue / Lyrics. */
@Composable
internal fun WaifuPillButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    background: Color = WaifuPalette.Surface,
    border: Color = WaifuPalette.Border,
    tint: Color = Color.White,
) {
    Row(
        modifier =
            modifier
                .clip(CircleShape)
                .background(background)
                .drawBehind {
                    drawRoundRect(
                        color = border,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f),
                        style = Stroke(width = 1.dp.toPx()),
                    )
                }.clickable(onClick = onClick)
                .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(
            text = label,
            color = tint,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}

@Composable
internal fun WaifuQueueLyricsRow(
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    background: Color = WaifuPalette.Surface,
    border: Color = WaifuPalette.Border,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WaifuPillButton(
            icon = SimpIcons.QueueMusic,
            label = "Queue",
            onClick = actions.onShowQueue,
            modifier = Modifier.weight(1f),
            background = background,
            border = border,
        )
        WaifuPillButton(
            icon = SimpIcons.Lyrics,
            label = "Lyrics",
            onClick = actions.onShowFullscreenLyrics,
            modifier = Modifier.weight(1f),
            background = background,
            border = border,
        )
    }
}

/** Like · add to playlist · queue · lyrics as a compact icon row. */
@Composable
internal fun WaifuIconActionRow(
    state: NowPlayingContentState,
    actions: NowPlayingContentActions,
    modifier: Modifier = Modifier,
    tint: Color = Color.White,
    background: Color = Color.Transparent,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WaifuLikeButton(state, actions, tint = tint, background = background)
        WaifuIconButton(SimpIcons.PlaylistAdd, "Add to playlist", actions.onShowAddToPlaylist, tint = tint, background = background)
        WaifuIconButton(SimpIcons.QueueMusic, "Queue", actions.onShowQueue, tint = tint, background = background)
        WaifuIconButton(SimpIcons.Lyrics, "Lyrics", actions.onShowFullscreenLyrics, tint = tint, background = background)
    }
}
