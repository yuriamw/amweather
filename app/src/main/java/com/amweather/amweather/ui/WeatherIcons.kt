/*
 * Copyright (C) 2026 yuriamw (https://github.com/yuriamw)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.amweather.amweather.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.amweather.amweather.data.isDaytime


// ---------------------------------------------------------------------------
// Dispatcher
// ---------------------------------------------------------------------------

@Composable
fun WeatherIcon(code: Int, modifier: Modifier = Modifier, size: Dp = 96.dp) {
    val isDayNow = isDaytime()
    when (code) {
        0 -> if (isDayNow) SunIcon(modifier, size) else MoonIcon(modifier, size)
        1 -> if (isDayNow) SunIcon(modifier, size) else MoonIcon(modifier, size)
        2 -> if (isDayNow) PartlyCloudyDayIcon(modifier, size) else PartlyCloudyNightIcon(modifier, size)
        3 -> if (isDayNow) OvercastDayIcon(modifier, size) else OvercastNightIcon(modifier, size)
        45, 48 -> FogIcon(modifier, size)
        51, 53, 55 -> DrizzleIcon(modifier, size)
        61, 63 -> RainIcon(modifier, size)
        65 -> HeavyRainIcon(modifier, size)
        71, 73, 75, 77 -> SnowIcon(modifier, size)
        80, 81, 82 -> RainIcon(modifier, size)
        85, 86 -> SnowIcon(modifier, size)
        95 -> ThunderstormIcon(modifier, size)
        96, 99 -> ThunderstormRainIcon(modifier, size)
        else -> if (isDayNow) SunIcon(modifier, size) else MoonIcon(modifier, size)
    }
}

// ---------------------------------------------------------------------------
// Color palette — inspired by Meteocons
// ---------------------------------------------------------------------------

private val SunYellow = Color(0xFFFBBF24)
private val SunYellowDark = Color(0xFFF59E0B)
private val CloudWhite = Color(0xFFF3F7FE)
private val CloudWhiteDark = Color(0xFFDEEAFB)
private val CloudGray = Color(0xFF9CA3AF)
private val CloudGrayDark = Color(0xFF6B7280)
private val RainBlue = Color(0xFF60A5FA)
private val RainBlueDark = Color(0xFF3B82F6)
private val SnowBlue = Color(0xFFBAE6FD)
private val SnowBlueDark = Color(0xFF7DD3FC)
private val LightningYellow = Color(0xFFFBBF24)
private val MoonColor = Color(0xFFFDE68A)
private val MoonColorDark = Color(0xFFFCD34D)
private val FogGray = Color(0xFFD1D5DB)
private val FogGrayDark = Color(0xFF9CA3AF)

// ---------------------------------------------------------------------------
// Shared drawing primitives
// ---------------------------------------------------------------------------

private fun DrawScope.drawSun(
    center: Offset,
    radius: Float,
    rayLength: Float,
    rayCount: Int = 8
) {
    // core circle
    drawCircle(
        color = SunYellow,
        radius = radius,
        center = center
    )
    drawCircle(
        color = SunYellowDark,
        radius = radius,
        center = center,
        style = Stroke(width = radius * 0.08f)
    )
    // rays
    val strokeWidth = radius * 0.18f
    for (i in 0 until rayCount) {
        val angle = Math.toRadians((i * 360.0 / rayCount)).toFloat()
        val startRadius = radius * 1.3f
        val endRadius = radius * 1.3f + rayLength
        val start = Offset(
            center.x + startRadius * kotlin.math.cos(angle),
            center.y + startRadius * kotlin.math.sin(angle)
        )
        val end = Offset(
            center.x + endRadius * kotlin.math.cos(angle),
            center.y + endRadius * kotlin.math.sin(angle)
        )
        drawLine(
            color = SunYellow,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawCloud(
    left: Float, top: Float,
    width: Float, height: Float,
    color: Color = CloudWhite,
    strokeColor: Color = CloudWhiteDark,
    strokeWidth: Float = 4f
) {
    val path = cloudPath(left, top, width, height)
    drawPath(path, color = color)
    drawPath(path, color = strokeColor, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
}

private fun cloudPath(
    left: Float, top: Float,
    width: Float, height: Float
): Path {
    val path = Path()
    val bottom = top + height
    val w = width
    val h = height

    // Cloud shape built entirely from cubic bezier curves
    // Start at bottom-left
    path.moveTo(left + w * 0.15f, bottom)

    // Left bump
    path.cubicTo(
        left + w * 0.05f, bottom,
        left, bottom - h * 0.4f,
        left + w * 0.15f, bottom - h * 0.5f
    )
    path.cubicTo(
        left + w * 0.18f, bottom - h * 0.55f,
        left + w * 0.20f, bottom - h * 0.62f,
        left + w * 0.28f, bottom - h * 0.65f
    )

    // Centre bump (tallest)
    path.cubicTo(
        left + w * 0.30f, bottom - h * 0.90f,
        left + w * 0.55f, bottom - h * 1.02f,
        left + w * 0.60f, bottom - h * 0.80f
    )

    // Right bump
    path.cubicTo(
        left + w * 0.65f, bottom - h * 0.70f,
        left + w * 0.70f, bottom - h * 0.65f,
        left + w * 0.75f, bottom - h * 0.62f
    )
    path.cubicTo(
        left + w * 0.85f, bottom - h * 0.58f,
        left + w * 1.00f, bottom - h * 0.35f,
        left + w * 0.88f, bottom
    )

    path.lineTo(left + w * 0.15f, bottom)
    path.close()
    return path
}

private fun DrawScope.drawRainDrops(
    cloudLeft: Float, cloudBottom: Float,
    cloudWidth: Float,
    drops: Int = 3,
    color: Color = RainBlue,
    dropLength: Float = 0f,
    spacing: Float = 0f
) {
    val actualSpacing = if (spacing == 0f) cloudWidth / (drops + 1) else spacing
    val actualLength = if (dropLength == 0f) cloudWidth * 0.12f else dropLength
    val strokeWidth = actualLength * 0.35f
    for (i in 1..drops) {
        val x = cloudLeft + actualSpacing * i
        val yStart = cloudBottom + actualLength * 0.5f
        val yEnd = cloudBottom + actualLength * 1.8f
        drawLine(
            color = color,
            start = Offset(x, yStart),
            end = Offset(x, yEnd),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun DrawScope.drawSnowDots(
    cloudLeft: Float, cloudBottom: Float,
    cloudWidth: Float,
    flakes: Int = 3,
    color: Color = SnowBlue,
    radius: Float = 0f
) {
    val spacing = cloudWidth / (flakes + 1)
    val actualRadius = if (radius == 0f) cloudWidth * 0.05f else radius
    for (i in 1..flakes) {
        val x = cloudLeft + spacing * i
        val y = cloudBottom + cloudWidth * 0.18f
        drawCircle(color = color, radius = actualRadius, center = Offset(x, y))
        drawCircle(
            color = color.copy(alpha = 0.5f),
            radius = actualRadius * 0.6f,
            center = Offset(x, y + cloudWidth * 0.15f)
        )
    }
}

private fun DrawScope.drawLightningBolt(
    centerX: Float, top: Float,
    height: Float,
    color: Color = LightningYellow
) {
    val path = Path().apply {
        val w = height * 0.5f
        moveTo(centerX + w * 0.2f, top)
        lineTo(centerX - w * 0.2f, top + height * 0.45f)
        lineTo(centerX + w * 0.1f, top + height * 0.45f)
        lineTo(centerX - w * 0.2f, top + height)
        lineTo(centerX + w * 0.4f, top + height * 0.52f)
        lineTo(centerX + w * 0.05f, top + height * 0.52f)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawMoon(center: Offset, radius: Float) {
    // crescent: full circle minus offset circle
    val path = Path().apply {
        addArc(
            oval = Rect(center.x - radius, center.y - radius,
                center.x + radius, center.y + radius),
            startAngleDegrees = 0f, sweepAngleDegrees = 360f
        )
    }
    drawPath(path, color = MoonColor)
    // cut out crescent with background-colored circle
    drawCircle(
        color = Color.Transparent,
        radius = radius * 0.75f,
        center = Offset(center.x + radius * 0.35f, center.y - radius * 0.2f)
    )
    drawCircle(
        color = MoonColorDark,
        radius = radius,
        center = center,
        style = Stroke(width = radius * 0.08f)
    )
    // overlay to carve crescent
    drawArc(
        color = Color.White.copy(alpha = 0f),
        startAngle = -60f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(center.x - radius * 0.1f, center.y - radius * 0.95f),
        size = Size(radius * 1.1f, radius * 1.9f)
    )
}

// ---------------------------------------------------------------------------
// Individual icons
// ---------------------------------------------------------------------------

@Composable
fun SunIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cx = s.width / 2f
        val cy = s.height / 2f
        val r = s.width * 0.22f
        val ray = s.width * 0.10f
        drawSun(Offset(cx, cy), r, ray)
    }
}

@Composable
fun MoonIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cx = s.width * 0.45f
        val cy = s.height * 0.45f
        val r = s.width * 0.28f

        // draw crescent using a path: full circle minus offset circle
        // achieved by drawing the outer circle and then the inner "bite"
        // using BlendMode is the cleanest approach
        val moonColor = Color(0xFFB0BEC5)       // blue-gray
        val moonDark = Color(0xFF78909C)         // darker outline
        val cresCenter = Offset(cx + r * 0.42f, cy - r * 0.20f)
        val cresRadius = r * 0.78f

        // outer moon circle
        drawCircle(color = moonColor, radius = r, center = Offset(cx, cy))

        // carve crescent by drawing background-colored circle on top
        // use the actual background color — Material3 surface
        drawCircle(
            color = Color(0xFFFAFAFA),
            radius = cresRadius,
            center = cresCenter
        )

        // outline of the moon
        drawCircle(
            color = moonDark,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.07f)
        )

        // small stars
        drawCircle(
            color = moonDark,
            radius = s.width * 0.03f,
            center = Offset(s.width * 0.78f, s.height * 0.20f)
        )
        drawCircle(
            color = moonDark.copy(alpha = 0.6f),
            radius = s.width * 0.018f,
            center = Offset(s.width * 0.82f, s.height * 0.40f)
        )
    }
}

@Composable
fun PartlyCloudyDayIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        // sun in top-right background
        drawSun(
            center = Offset(s.width * 0.62f, s.height * 0.35f),
            radius = s.width * 0.18f,
            rayLength = s.width * 0.07f,
            rayCount = 8
        )
        // cloud in foreground bottom-left
        drawCloud(
            left = s.width * 0.04f,
            top = s.height * 0.42f,
            width = s.width * 0.82f,
            height = s.height * 0.40f,
            strokeWidth = s.width * 0.025f
        )
    }
}

@Composable
fun PartlyCloudyNightIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val moonColor = Color(0xFFB0BEC5)
        val moonDark = Color(0xFF78909C)
        val bgColor = Color(0xFFFAFAFA)

        val cx = s.width * 0.62f
        val cy = s.height * 0.30f
        val r = s.width * 0.18f

        // moon circle
        drawCircle(color = moonColor, radius = r, center = Offset(cx, cy))
        // carve crescent
        drawCircle(
            color = bgColor,
            radius = r * 0.78f,
            center = Offset(cx + r * 0.42f, cy - r * 0.20f)
        )
        // outline
        drawCircle(
            color = moonDark,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.07f)
        )

        // cloud foreground
        drawCloud(
            left = s.width * 0.04f,
            top = s.height * 0.42f,
            width = s.width * 0.82f,
            height = s.height * 0.40f,
            strokeWidth = s.width * 0.025f
        )
    }
}

@Composable
fun OvercastNightIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val moonColor = Color(0xFFB0BEC5)
        val moonDark = Color(0xFF78909C)
        val bgColor = Color(0xFFFAFAFA)

        val cx = s.width * 0.50f
        val cy = s.height * 0.32f
        val r = s.width * 0.14f

        // faint moon peeking behind cloud
        drawCircle(color = moonColor.copy(alpha = 0.6f), radius = r, center = Offset(cx, cy))
        drawCircle(
            color = bgColor,
            radius = r * 0.78f,
            center = Offset(cx + r * 0.42f, cy - r * 0.20f)
        )
        drawCircle(
            color = moonDark.copy(alpha = 0.6f),
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = r * 0.07f)
        )

        // large gray overcast cloud
        drawCloud(
            left = s.width * 0.04f,
            top = s.height * 0.35f,
            width = s.width * 0.88f,
            height = s.height * 0.46f,
            color = CloudGray,
            strokeColor = CloudGrayDark,
            strokeWidth = s.width * 0.025f
        )
    }
}


@Composable
fun OvercastDayIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        // faint sun peeking
        drawSun(
            center = Offset(s.width * 0.5f, s.height * 0.38f),
            radius = s.width * 0.14f,
            rayLength = s.width * 0.05f,
            rayCount = 8
        )
        // large gray overcast cloud
        drawCloud(
            left = s.width * 0.04f,
            top = s.height * 0.35f,
            width = s.width * 0.88f,
            height = s.height * 0.46f,
            color = CloudGray,
            strokeColor = CloudGrayDark,
            strokeWidth = s.width * 0.025f
        )
    }
}

@Composable
fun FogIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val strokeWidth = s.width * 0.07f
        val cap = StrokeCap.Round
        // five horizontal fog lines decreasing in opacity
        val lines = listOf(
            Triple(0.15f, 0.85f, 0.25f),
            Triple(0.08f, 0.92f, 0.38f),
            Triple(0.15f, 0.85f, 0.51f),
            Triple(0.08f, 0.92f, 0.64f),
            Triple(0.15f, 0.85f, 0.77f),
        )
        lines.forEachIndexed { i, (startX, endX, y) ->
            drawLine(
                color = FogGray.copy(alpha = 1f - i * 0.15f),
                start = Offset(s.width * startX, s.height * y),
                end = Offset(s.width * endX, s.height * y),
                strokeWidth = strokeWidth,
                cap = cap
            )
        }
    }
}

@Composable
fun DrizzleIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.08f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.44f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            strokeWidth = s.width * 0.025f
        )
        // light drizzle: 4 short drops
        drawRainDrops(
            cloudLeft = cLeft,
            cloudBottom = cTop + cHeight,
            cloudWidth = cWidth,
            drops = 4,
            color = RainBlue.copy(alpha = 0.7f),
            dropLength = cWidth * 0.10f
        )
    }
}

@Composable
fun RainIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.06f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.42f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            strokeWidth = s.width * 0.025f
        )
        drawRainDrops(
            cloudLeft = cLeft,
            cloudBottom = cTop + cHeight,
            cloudWidth = cWidth,
            drops = 3,
            color = RainBlue,
            dropLength = cWidth * 0.14f
        )
    }
}

@Composable
fun HeavyRainIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.06f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.40f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            color = CloudGray,
            strokeColor = CloudGrayDark,
            strokeWidth = s.width * 0.025f
        )
        // two rows of heavy drops
        drawRainDrops(
            cloudLeft = cLeft,
            cloudBottom = cTop + cHeight,
            cloudWidth = cWidth,
            drops = 4,
            color = RainBlueDark,
            dropLength = cWidth * 0.16f
        )
        drawRainDrops(
            cloudLeft = cLeft + cWidth * 0.12f,
            cloudBottom = cTop + cHeight + cWidth * 0.18f,
            cloudWidth = cWidth * 0.76f,
            drops = 3,
            color = RainBlueDark.copy(alpha = 0.7f),
            dropLength = cWidth * 0.13f
        )
    }
}

@Composable
fun SnowIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.06f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.42f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            color = CloudWhite,
            strokeColor = CloudWhiteDark,
            strokeWidth = s.width * 0.025f
        )
        // snowflake dots in two rows
        val spacing = cWidth / 4f
        val dotR = s.width * 0.045f
        val row1Y = cTop + cHeight + s.height * 0.12f
        val row2Y = cTop + cHeight + s.height * 0.26f
        for (i in 1..3) {
            drawCircle(
                color = SnowBlue,
                radius = dotR,
                center = Offset(cLeft + spacing * i, row1Y)
            )
        }
        for (i in 0..2) {
            drawCircle(
                color = SnowBlueDark,
                radius = dotR * 0.75f,
                center = Offset(cLeft + spacing * 0.5f + spacing * i, row2Y)
            )
        }
    }
}

@Composable
fun ThunderstormIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.06f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.40f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            color = CloudGray,
            strokeColor = CloudGrayDark,
            strokeWidth = s.width * 0.025f
        )
        drawLightningBolt(
            centerX = s.width * 0.50f,
            top = cTop + cHeight + s.height * 0.04f,
            height = s.height * 0.44f
        )
    }
}

@Composable
fun ThunderstormRainIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val cLeft = s.width * 0.06f
        val cTop = s.height * 0.04f
        val cWidth = s.width * 0.86f
        val cHeight = s.height * 0.38f
        drawCloud(
            left = cLeft, top = cTop,
            width = cWidth, height = cHeight,
            color = CloudGray,
            strokeColor = CloudGrayDark,
            strokeWidth = s.width * 0.025f
        )
        // rain on left side
        drawRainDrops(
            cloudLeft = cLeft,
            cloudBottom = cTop + cHeight,
            cloudWidth = cWidth * 0.45f,
            drops = 2,
            color = RainBlue,
            dropLength = cWidth * 0.12f
        )
        // lightning on right side
        drawLightningBolt(
            centerX = s.width * 0.68f,
            top = cTop + cHeight + s.height * 0.04f,
            height = s.height * 0.40f
        )
    }
}

@Composable
fun WindIcon(modifier: Modifier = Modifier, size: Dp = 96.dp) {
    Canvas(modifier = modifier.size(size)) {
        val s = this.size
        val strokeWidth = s.width * 0.07f

        // three curved wind lines
        data class WindLine(val startX: Float, val endX: Float, val y: Float, val alpha: Float)
        val lines = listOf(
            WindLine(0.08f, 0.75f, 0.30f, 1.0f),
            WindLine(0.08f, 0.88f, 0.50f, 0.85f),
            WindLine(0.08f, 0.68f, 0.70f, 0.65f),
        )
        lines.forEach { line ->
            // draw as arc-like curve using path
            val path = Path().apply {
                val startX = s.width * line.startX
                val endX = s.width * line.endX
                val y = s.height * line.y
                val curl = s.height * 0.08f
                moveTo(startX, y)
                cubicTo(
                    startX + (endX - startX) * 0.5f, y,
                    endX - curl, y - curl,
                    endX, y
                )
                cubicTo(
                    endX + curl * 0.5f, y + curl * 0.3f,
                    endX, y + curl,
                    endX - curl * 1.2f, y + curl * 0.8f
                )
            }
            drawPath(
                path = path,
                color = FogGray.copy(alpha = line.alpha),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}
