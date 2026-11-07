package com.example.hmidashboard.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hmidashboard.R
import com.example.hmidashboard.domain.KillSwitchStatus
import com.example.hmidashboard.domain.LinkStatus
import com.example.hmidashboard.domain.MotorStatus
import com.example.hmidashboard.domain.RideState
import com.example.hmidashboard.domain.SpeedStatus
import com.example.hmidashboard.domain.VehicleUiState
import com.example.hmidashboard.ui.theme.HmiColors
import com.example.hmidashboard.ui.theme.HmiTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private const val STRIP_MAX_KMH = 100f

/**
 * Modern High-Performance HMI Dashboard for Electric Mobility.
 * Features luminous neon glow accents, tactile glassmorphism cards,
 * dynamic speed gauge with tick markers, and reactive warning states.
 */
@Composable
fun DashboardScreen(state: VehicleUiState, modifier: Modifier = Modifier) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF141C26),
                        HmiColors.Background,
                        Color(0xFF040608),
                    ),
                    radius = 1800f,
                ),
            ),
    ) {
        val h = maxHeight
        val w = maxWidth
        val accent = accentFor(state.rideState)

        // Subtle ambient background grid/glow line
        BackgroundGlowLines(accent = accent, modifier = Modifier.fillMaxSize())

        StatusBar(
            link = state.link,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 10.dp),
        )

        // Glowing speed dash strip with automotive wings & ticks
        DashStrip(
            speedKmh = state.speedKmh,
            accent = accent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = h * 0.53f)
                .width(w * 0.84f)
                .height(h * 0.30f),
        )

        HexLogo(
            color = accent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = h * 0.08f)
                .size(h * 0.085f),
        )

        RidePanel(
            rideState = state.rideState,
            link = state.link,
            accent = accent,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = h * 0.18f)
                .width(w * 0.34f)
                .height(h * 0.125f),
        )

        SpeedReadout(
            state = state,
            fontSize = (h.value * 0.30f).sp,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = h * 0.27f),
        )

        MetricsRow(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = h * 0.825f),
        )

        DriveStateRibbon(
            rideState = state.rideState,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .offset(y = -(h * 0.06f)),
        )

        KillSwitchTile(
            status = state.killSwitch,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = h * 0.28f)
                .padding(end = 26.dp)
                .width(w * 0.21f),
        )

        BottomBar(
            motor = state.motor,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
        )
    }
}

private fun accentFor(rideState: RideState): Color = when (rideState) {
    RideState.MOTOR_DISABLED -> HmiColors.Red
    RideState.UNAVAILABLE -> HmiColors.Grey
    RideState.ACCELERATING -> HmiColors.Orange
    RideState.RIDING -> HmiColors.Cyan
    else -> HmiColors.Green
}

// ---------------------------------------------------------------- background ambient

@Composable
private fun BackgroundGlowLines(accent: Color, modifier: Modifier = Modifier) {
    val animAlpha by animateFloatAsState(
        targetValue = if (accent == HmiColors.Red) 0.5f else 0.25f,
        animationSpec = tween(600),
        label = "ambientGlow",
    )
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        // Subtle top horizon line
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(Color.Transparent, Color(0xFF283648).copy(alpha = animAlpha), Color.Transparent),
            ),
            start = Offset(w * 0.1f, h * 0.07f),
            end = Offset(w * 0.9f, h * 0.07f),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

// ---------------------------------------------------------------- status bar

@Composable
private fun StatusBar(link: LinkStatus, modifier: Modifier = Modifier) {
    val clock by produceState(initialValue = formatNow()) {
        while (true) {
            delay(15_000)
            value = formatNow()
        }
    }
    val (linkText, linkColor) = when (link) {
        LinkStatus.LIVE -> stringResource(R.string.link_live) to HmiColors.Green
        LinkStatus.WAITING -> stringResource(R.string.link_waiting) to HmiColors.Grey
        LinkStatus.STALE -> stringResource(R.string.link_stale) to HmiColors.Amber
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Connected Phone pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(HmiColors.SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp),
        ) {
            Box(
                Modifier
                    .size(8.dp)
                    .background(Color(0xFF3B82F6), CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                stringResource(R.string.phone_status),
                color = HmiColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
        }

        // Live / Telemetry status, clock, connectivity
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .background(HmiColors.SurfaceDark.copy(alpha = 0.85f), RoundedCornerShape(20.dp))
                .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 5.dp),
        ) {
            // Pulsing live indicator dot
            Box(
                Modifier
                    .size(7.dp)
                    .background(linkColor, CircleShape),
            )
            Spacer(Modifier.width(7.dp))
            Text(
                text = linkText,
                color = linkColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Spacer(Modifier.width(16.dp))
            Text(
                clock,
                color = HmiColors.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.width(14.dp))
            Text(
                text = "5G",
                color = Color.Black,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier
                    .background(HmiColors.Cyan, RoundedCornerShape(4.dp))
                    .padding(horizontal = 5.dp, vertical = 1.dp),
            )
            Spacer(Modifier.width(8.dp))
            SignalBars(Modifier.size(width = 18.dp, height = 13.dp))
        }
    }
}

private fun formatNow(): String =
    SimpleDateFormat("EEE d MMM  hh:mm a", Locale.getDefault()).format(Date())

@Composable
private fun SignalBars(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val barW = size.width / 7f
        for (i in 0..3) {
            val barH = size.height * (0.35f + 0.22f * i)
            drawRoundRect(
                color = if (i < 3) Color.White else HmiColors.Grey.copy(alpha = 0.5f),
                topLeft = Offset(i * 2f * barW, size.height - barH),
                size = Size(barW * 1.5f, barH),
                cornerRadius = CornerRadius(2f, 2f),
            )
        }
    }
}

// ------------------------------------------------------------------ centre

@Composable
private fun HexLogo(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        val hex = Path()
        for (i in 0..5) {
            val a = Math.toRadians((60.0 * i) - 90.0)
            val x = c.x + r * cos(a).toFloat()
            val y = c.y + r * sin(a).toFloat()
            if (i == 0) hex.moveTo(x, y) else hex.lineTo(x, y)
        }
        hex.close()

        // Outer glow
        drawPath(hex, color.copy(alpha = 0.25f), style = Stroke(width = 6.dp.toPx()))
        // Main outline
        drawPath(hex, color, style = Stroke(width = 2.dp.toPx()))

        // Inner solid diamond
        val d = r * 0.45f
        val diamond = Path().apply {
            moveTo(c.x, c.y - d)
            lineTo(c.x + d, c.y)
            lineTo(c.x, c.y + d)
            lineTo(c.x - d, c.y)
            close()
        }
        drawPath(diamond, Brush.radialGradient(listOf(color.copy(alpha = 0.9f), color.copy(alpha = 0.3f))))
    }
}

@Composable
private fun RidePanel(
    rideState: RideState,
    link: LinkStatus,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    val label = rideLabel(rideState)
    val accentAnimated by animateColorAsState(targetValue = accent, label = "ridePanelAccent")

    Box(
        modifier = modifier
            .shadow(12.dp, RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        HmiColors.SurfaceCard,
                        HmiColors.SurfaceDark,
                    ),
                ),
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            )
            .border(
                1.dp,
                Brush.verticalGradient(listOf(HmiColors.SurfaceCardBorder, Color.Transparent)),
                RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            ),
    ) {
        // High-contrast luminous top bar
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.85f)
                .height(3.dp)
                .background(
                    if (link == LinkStatus.LIVE) accentAnimated else HmiColors.Grey,
                    RoundedCornerShape(2.dp),
                ),
        )

        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                color = if (rideState == RideState.UNAVAILABLE) HmiColors.Grey else accentAnimated,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.5.sp,
                maxLines = 1,
                modifier = Modifier
                    .background(Color(0xFF0C1015), RoundedCornerShape(50))
                    .border(1.dp, accentAnimated.copy(alpha = 0.35f), RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 6.dp),
            )
        }
    }
}

@Composable
private fun SpeedReadout(
    state: VehicleUiState,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
) {
    val speedTarget = (state.speedKmh ?: 0).toFloat()
    val animatedSpeed by animateFloatAsState(
        targetValue = speedTarget,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "animatedSpeed",
    )
    val displaySpeed = if (state.speedKmh == null) {
        stringResource(R.string.speed_placeholder)
    } else {
        animatedSpeed.toInt().toString()
    }

    val warning = when (state.speedStatus) {
        SpeedStatus.OK -> null
        SpeedStatus.INVALID -> stringResource(R.string.speed_invalid)
        SpeedStatus.MISSING -> stringResource(R.string.speed_missing)
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = displaySpeed,
            color = if (state.speedKmh == null) HmiColors.Grey else HmiColors.TextPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.SansSerif,
            lineHeight = fontSize,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(2.dp))
        if (warning != null) {
            Text(
                text = warning,
                color = HmiColors.Amber,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color(0x33FFA000), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(HmiColors.SurfaceDark.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                    .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 2.dp),
            ) {
                Text(
                    text = stringResource(R.string.unit_kmh),
                    color = HmiColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}

/** The "dash" shape with automotive tick markers, wings and a multi-layer luminous neon speed strip. */
@Composable
private fun DashStrip(speedKmh: Int?, accent: Color, modifier: Modifier = Modifier) {
    val fraction = ((speedKmh ?: 0) / STRIP_MAX_KMH).coerceIn(0f, 1f)
    val animated by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "stripFraction",
    )

    Canvas(modifier) {
        val w = size.width
        val h = size.height

        // Futuristic faceted wing body
        val body = Path().apply {
            moveTo(0.01f * w, 0.95f * h)
            lineTo(0.24f * w, 0.38f * h)
            lineTo(0.38f * w, 0.10f * h)
            lineTo(0.62f * w, 0.10f * h)
            lineTo(0.76f * w, 0.38f * h)
            lineTo(0.99f * w, 0.95f * h)
            close()
        }
        drawPath(
            body,
            Brush.verticalGradient(
                listOf(
                    Color(0xFF222B36),
                    Color(0xFF141920),
                    Color(0xFF0C0F14),
                ),
            ),
        )

        // Wing contour border highlight
        drawPath(
            body,
            Brush.verticalGradient(
                listOf(
                    Color(0xFF384658),
                    Color(0x22384658),
                    Color.Transparent,
                ),
            ),
            style = Stroke(width = 1.2.dp.toPx()),
        )

        // Perforation dot matrices on left & right wings
        val dotRadius = 1.5.dp.toPx()
        for (row in 0..3) {
            val y = (0.42f + row * 0.11f) * h
            for (col in 0..10) {
                val offsetX = 0.04f * w + col * 0.014f * w * (1f + row * 0.30f)
                val dotAlpha = (1f - col * 0.07f).coerceIn(0.2f, 0.8f)
                drawCircle(
                    Color(0xFF4C5B6E).copy(alpha = dotAlpha),
                    dotRadius,
                    Offset(0.08f * w + offsetX + row * 0.008f * w, y),
                )
                drawCircle(
                    Color(0xFF4C5B6E).copy(alpha = dotAlpha),
                    dotRadius,
                    Offset(0.92f * w - offsetX - row * 0.008f * w, y),
                )
            }
        }

        // Speed Track Geometry
        val stripH = 0.16f * h
        val stripTop = 0.72f * h
        val fullW = 0.92f * w
        val left = (w - fullW) / 2f
        val corner = CornerRadius(stripH / 2f, stripH / 2f)

        // Track background groove
        drawRoundRect(
            color = HmiColors.Track,
            topLeft = Offset(left, stripTop),
            size = Size(fullW, stripH),
            cornerRadius = corner,
        )
        drawRoundRect(
            color = Color(0xFF131820),
            topLeft = Offset(left + 1.dp.toPx(), stripTop + 1.dp.toPx()),
            size = Size(fullW - 2.dp.toPx(), stripH - 2.dp.toPx()),
            cornerRadius = corner,
        )

        // Segmented tick marks along top of gauge
        val tickCount = 20
        val tickGap = fullW / tickCount
        for (i in 0..tickCount) {
            val tickX = left + i * tickGap
            val isMajor = i % 5 == 0
            val tickH = if (isMajor) 0.08f * h else 0.04f * h
            val tickY = stripTop - tickH - 4.dp.toPx()
            drawLine(
                color = if (isMajor) HmiColors.TextSecondary else HmiColors.TextMuted,
                start = Offset(tickX, tickY),
                end = Offset(tickX, stripTop - 4.dp.toPx()),
                strokeWidth = (if (isMajor) 1.5.dp else 1.dp).toPx(),
                cap = StrokeCap.Round,
            )
        }

        // Active glow & neon core
        val activeW = fullW * animated
        if (activeW > stripH) {
            val glow = glowColors(accent)

            // Layer 1: Ambient outer bloom
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = glow.map { it.copy(alpha = 0.35f) },
                    startX = (w - activeW) / 2f - 4.dp.toPx(),
                    endX = (w + activeW) / 2f + 4.dp.toPx(),
                ),
                topLeft = Offset((w - activeW) / 2f - 3.dp.toPx(), stripTop - 2.dp.toPx()),
                size = Size(activeW + 6.dp.toPx(), stripH + 4.dp.toPx()),
                cornerRadius = CornerRadius((stripH + 4.dp.toPx()) / 2f, (stripH + 4.dp.toPx()) / 2f),
            )

            // Layer 2: Core neon beam
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = glow,
                    startX = (w - activeW) / 2f,
                    endX = (w + activeW) / 2f,
                ),
                topLeft = Offset((w - activeW) / 2f, stripTop),
                size = Size(activeW, stripH),
                cornerRadius = corner,
            )

            // Layer 3: Inner white streak for high-speed intensity
            if (animated > 0.4f) {
                val intenseW = activeW * 0.7f
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color.White.copy(alpha = 0.45f), Color.Transparent),
                    ),
                    topLeft = Offset((w - intenseW) / 2f, stripTop + stripH * 0.25f),
                    size = Size(intenseW, stripH * 0.5f),
                    cornerRadius = CornerRadius(stripH * 0.25f, stripH * 0.25f),
                )
            }
        } else if (accent == HmiColors.Red) {
            // motor disabled: keep a visible red warning core even at 0 km/h
            val warningW = fullW * 0.22f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(HmiColors.Red.copy(alpha = 0.4f), HmiColors.Red, HmiColors.Red.copy(alpha = 0.4f)),
                ),
                topLeft = Offset((w - warningW) / 2f, stripTop),
                size = Size(warningW, stripH),
                cornerRadius = corner,
            )
        }
    }
}

private fun glowColors(accent: Color): List<Color> = when (accent) {
    HmiColors.Red -> listOf(Color(0xFFFF7A88), HmiColors.Red, Color(0xFFFF4D63), HmiColors.Red, Color(0xFFFF7A88))
    HmiColors.Grey -> listOf(HmiColors.Grey, HmiColors.Grey)
    HmiColors.Cyan -> listOf(Color(0xFF80F3FF), HmiColors.Cyan, Color(0xFF00B0FF), HmiColors.Cyan, Color(0xFF80F3FF))
    HmiColors.Green -> listOf(Color(0xFF80FFBF), HmiColors.Green, Color(0xFF00C853), HmiColors.Green, Color(0xFF80FFBF))
    else -> listOf(Color(0xFFFFD54F), HmiColors.Orange, Color(0xFFFF5722), HmiColors.Orange, Color(0xFFFFD54F))
}

@Composable
private fun MetricsRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(HmiColors.SurfaceDark.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
            .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(stringResource(R.string.odo_label), color = HmiColors.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.odo_value), color = HmiColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(16.dp))
        Box(
            Modifier
                .size(4.dp)
                .background(HmiColors.TextMuted, CircleShape),
        )
        Spacer(Modifier.width(16.dp))
        Text(stringResource(R.string.range_label), color = HmiColors.Cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(6.dp))
        Text(stringResource(R.string.range_value), color = HmiColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// -------------------------------------------------------------------- sides

/** Modern side drive mode indicator ribbon (P - R - N - D style automotive strip). */
@Composable
private fun DriveStateRibbon(rideState: RideState, modifier: Modifier = Modifier) {
    val currentMode = when (rideState) {
        RideState.PARKED -> "P"
        RideState.READY -> "N"
        RideState.RIDING, RideState.ACCELERATING, RideState.COASTING -> "D"
        RideState.MOTOR_DISABLED -> "!"
        RideState.UNAVAILABLE -> "--"
    }

    val modes = listOf("P", "R", "N", "D")

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
            .background(
                Brush.horizontalGradient(listOf(HmiColors.SurfaceCard, HmiColors.SurfaceDark)),
            )
            .border(
                1.dp,
                HmiColors.SurfaceCardBorder,
                RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
            )
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        modes.forEach { mode ->
            val isSelected = currentMode == mode
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        if (isSelected) HmiColors.Green else Color.Transparent,
                        RoundedCornerShape(8.dp),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = mode,
                    color = if (isSelected) Color.Black else HmiColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                )
            }
        }
    }
}

/** Right-hand status tile. ACTIVE = solid red with a cross; INACTIVE = dark with a green tick. */
@Composable
private fun KillSwitchTile(status: KillSwitchStatus, modifier: Modifier = Modifier) {
    val (value, accent) = when (status) {
        KillSwitchStatus.ACTIVE -> stringResource(R.string.kill_active) to HmiColors.Red
        KillSwitchStatus.INACTIVE -> stringResource(R.string.kill_inactive) to HmiColors.Green
        KillSwitchStatus.UNKNOWN -> stringResource(R.string.kill_unknown) to HmiColors.Amber
    }
    val active = status == KillSwitchStatus.ACTIVE

    // Pulsing warning border when kill switch is ACTIVE
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulseAlpha",
    )

    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .shadow(if (active) 16.dp else 4.dp, shape, ambientColor = if (active) HmiColors.Red else Color.Black)
            .background(
                if (active) {
                    Brush.verticalGradient(listOf(Color(0xFFE52536), Color(0xFF8F0F1B)))
                } else {
                    Brush.verticalGradient(listOf(HmiColors.SurfaceCard, HmiColors.SurfaceDark))
                },
                shape,
            )
            .border(
                width = if (active) 2.5.dp else 1.2.dp,
                color = if (active) HmiColors.Red.copy(alpha = pulseBorder) else HmiColors.SurfaceCardBorder,
                shape = shape,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        KillSwitchIcon(status, accent, Modifier.size(36.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.label_kill_switch),
                color = if (active) Color.White.copy(alpha = 0.85f) else HmiColors.TextSecondary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
            )
            Text(
                text = value,
                color = if (active) Color.White else accent,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
            )
        }
    }
}

@Composable
private fun KillSwitchIcon(status: KillSwitchStatus, accent: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f
        val sw = 3.dp.toPx()
        when (status) {
            KillSwitchStatus.ACTIVE -> {
                drawCircle(Color.White, r)
                val d = r * 0.42f
                val s = Stroke(width = sw, cap = StrokeCap.Round)
                drawLine(HmiColors.Red, Offset(c.x - d, c.y - d), Offset(c.x + d, c.y + d), s.width, StrokeCap.Round)
                drawLine(HmiColors.Red, Offset(c.x + d, c.y - d), Offset(c.x - d, c.y + d), s.width, StrokeCap.Round)
            }
            KillSwitchStatus.INACTIVE -> {
                drawCircle(accent.copy(alpha = 0.15f), r)
                drawCircle(accent, r - sw / 2f, style = Stroke(width = sw))
                val tick = Path().apply {
                    moveTo(c.x - r * 0.40f, c.y)
                    lineTo(c.x - r * 0.08f, c.y + r * 0.32f)
                    lineTo(c.x + r * 0.42f, c.y - r * 0.30f)
                }
                drawPath(tick, accent, style = Stroke(width = sw, cap = StrokeCap.Round))
            }
            KillSwitchStatus.UNKNOWN -> {
                drawCircle(accent, r - sw / 2f, style = Stroke(width = sw))
                drawLine(accent, Offset(c.x - r * 0.4f, c.y), Offset(c.x + r * 0.4f, c.y), sw, StrokeCap.Round)
            }
        }
    }
}

// ------------------------------------------------------------------- bottom

@Composable
private fun BottomBar(motor: MotorStatus, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundButton { GridIcon(Modifier.size(18.dp)) }
            Spacer(Modifier.width(10.dp))
            RoundButton { NavIcon(Modifier.size(18.dp)) }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            BatteryPill()
            Spacer(Modifier.width(12.dp))
            MotorPill(motor)
        }
    }
}

@Composable
private fun RoundButton(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 52.dp, height = 36.dp)
            .background(HmiColors.SurfaceDark, RoundedCornerShape(50))
            .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(50)),
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun GridIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val g = size.width * 0.12f
        val s = (size.width - g) / 2f
        val r = CornerRadius(2.5f, 2.5f)
        drawRoundRect(Color.White, Offset(0f, 0f), Size(s, s), r)
        drawRoundRect(Color.White, Offset(s + g, 0f), Size(s, s), r)
        drawRoundRect(Color.White, Offset(0f, s + g), Size(s, s), r)
        drawRoundRect(Color.White, Offset(s + g, s + g), Size(s, s), r)
    }
}

@Composable
private fun NavIcon(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val p = Path().apply {
            moveTo(size.width * 0.5f, 0f)
            lineTo(size.width, size.height)
            lineTo(size.width * 0.5f, size.height * 0.72f)
            lineTo(0f, size.height)
            close()
        }
        drawPath(p, Color.White)
    }
}

@Composable
private fun BatteryPill() {
    Row(
        modifier = Modifier
            .background(HmiColors.SurfaceDark, RoundedCornerShape(50))
            .border(1.dp, HmiColors.SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(Modifier.size(width = 22.dp, height = 11.dp)) {
            drawRoundRect(HmiColors.Green, Offset.Zero, Size(size.width * 0.88f, size.height), CornerRadius(3f, 3f))
            drawRoundRect(
                HmiColors.Green,
                Offset(size.width * 0.90f, size.height * 0.28f),
                Size(size.width * 0.10f, size.height * 0.44f),
                CornerRadius(1.5f, 1.5f),
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.battery_value),
            color = HmiColors.Green,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

/** Bottom-right motor status pill: green "ON", grey "OFF", amber "--". */
@Composable
private fun MotorPill(motor: MotorStatus) {
    val (text, accent, container) = when (motor) {
        MotorStatus.ON -> Triple(stringResource(R.string.motor_on), HmiColors.Green, HmiColors.GreenDim)
        MotorStatus.OFF -> Triple(stringResource(R.string.motor_off), HmiColors.TextSecondary, HmiColors.SurfaceDark)
        MotorStatus.UNKNOWN -> Triple(stringResource(R.string.motor_unknown), HmiColors.Amber, Color(0xFF2E2210))
    }
    Row(
        modifier = Modifier
            .background(container, RoundedCornerShape(50))
            .border(1.dp, if (motor == MotorStatus.ON) HmiColors.Green.copy(alpha = 0.5f) else HmiColors.SurfaceCardBorder, RoundedCornerShape(50))
            .padding(horizontal = 16.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PowerIcon(accent, Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(
            text = stringResource(R.string.label_motor) + " " + text,
            color = accent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
        )
    }
}

@Composable
private fun PowerIcon(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val sw = 2.dp.toPx()
        val inset = sw
        drawArc(
            color = color,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = Size(size.width - 2 * inset, size.height - 2 * inset),
            style = Stroke(width = sw, cap = StrokeCap.Round),
        )
        drawLine(color, Offset(size.width / 2f, 0f), Offset(size.width / 2f, size.height * 0.5f), sw, StrokeCap.Round)
    }
}

@Composable
private fun rideLabel(rideState: RideState): String = when (rideState) {
    RideState.PARKED -> stringResource(R.string.ride_parked)
    RideState.READY -> stringResource(R.string.ride_ready)
    RideState.RIDING -> stringResource(R.string.ride_riding)
    RideState.ACCELERATING -> stringResource(R.string.ride_accelerating)
    RideState.COASTING -> stringResource(R.string.ride_coasting)
    RideState.MOTOR_DISABLED -> stringResource(R.string.ride_motor_disabled)
    RideState.UNAVAILABLE -> stringResource(R.string.ride_unavailable)
}

@Preview(widthDp = 900, heightDp = 412, showBackground = true)
@Composable
private fun DashboardPreview() {
    HmiTheme {
        DashboardScreen(
            VehicleUiState(
                speedKmh = 50,
                speedStatus = SpeedStatus.OK,
                motor = MotorStatus.ON,
                killSwitch = KillSwitchStatus.INACTIVE,
                rideState = RideState.ACCELERATING,
                link = LinkStatus.LIVE,
            ),
        )
    }
}

@Preview(widthDp = 900, heightDp = 412, showBackground = true)
@Composable
private fun DashboardDisabledPreview() {
    HmiTheme {
        DashboardScreen(
            VehicleUiState(
                speedKmh = 0,
                speedStatus = SpeedStatus.OK,
                motor = MotorStatus.OFF,
                killSwitch = KillSwitchStatus.ACTIVE,
                rideState = RideState.MOTOR_DISABLED,
                link = LinkStatus.LIVE,
            ),
        )
    }
}

