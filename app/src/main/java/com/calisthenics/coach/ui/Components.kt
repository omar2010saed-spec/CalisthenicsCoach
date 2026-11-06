package com.calisthenics.coach.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.Anim
import com.calisthenics.coach.data.Equip
import com.calisthenics.coach.data.Exercise
import kotlin.math.max

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val m = if (onClick != null) modifier.clip(RoundedCornerShape(20.dp)).clickable { onClick() } else modifier
    Card(
        modifier = m.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun Pill(text: String, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.16f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatBox(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
fun BulletList(items: List<String>, bullet: String = "•") {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach {
            Row {
                Text(bullet, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun WarningBox(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))
            .padding(12.dp)
    ) {
        Text("⚠️", modifier = Modifier.padding(end = 8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun LabeledBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(top = 10.dp)) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleSmall
        )
        Column(modifier = Modifier.padding(top = 4.dp), content = content)
    }
}

/**
 * Animated stick-figure illustration drawn locally with Canvas. It is a simplified guide for the movement path,
 * so there are no remote or bundled image files.
 */
@Composable
fun StickFigure(anim: Anim, modifier: Modifier = Modifier, animate: Boolean = true) {
    val transition = rememberInfiniteTransition(label = "figure")
    val moving by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500, easing = LinearEasing), RepeatMode.Reverse),
        label = "t"
    )
    val t = if (animate) moving else 0f
    val body = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.primary
    val ring = Amber

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        fun px(i: Int): Float = anim.a.p[i * 2] + (anim.b.p[i * 2] - anim.a.p[i * 2]) * t
        fun py(i: Int): Float = anim.a.p[i * 2 + 1] + (anim.b.p[i * 2 + 1] - anim.a.p[i * 2 + 1]) * t
        fun pt(i: Int) = Offset(px(i) * w, py(i) * h)

        val u = max(1f, minOf(w, h))
        drawLine(
            color = body.copy(alpha = 0.25f),
            start = Offset(0.03f * w, 0.9f * h),
            end = Offset(0.97f * w, 0.9f * h),
            strokeWidth = u * 0.012f,
            cap = StrokeCap.Round
        )
        val limb = u * 0.032f
        val torso = u * 0.042f
        // torso
        drawLine(body, pt(1), pt(4), strokeWidth = torso, cap = StrokeCap.Round)
        // arm
        drawLine(accent, pt(1), pt(2), strokeWidth = limb, cap = StrokeCap.Round)
        drawLine(accent, pt(2), pt(3), strokeWidth = limb, cap = StrokeCap.Round)
        // leg
        drawLine(body, pt(4), pt(5), strokeWidth = limb, cap = StrokeCap.Round)
        drawLine(body, pt(5), pt(6), strokeWidth = limb, cap = StrokeCap.Round)
        // head
        drawLine(body, pt(0), pt(1), strokeWidth = limb * 0.6f, cap = StrokeCap.Round)
        drawCircle(accent, radius = u * 0.055f, center = pt(0))
        anim.bandAt?.let { i ->
            drawCircle(ring, radius = u * 0.06f, center = pt(i), style = Stroke(width = u * 0.016f))
        }
    }
}

@Composable
fun ExerciseIllustration(e: Exercise, modifier: Modifier = Modifier, animate: Boolean = true) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        StickFigure(
            anim = e.anim,
            animate = animate,
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        )
        if (e.equip == Equip.BAND) {
            Pill(
                "حلقة مقاومة",
                color = Amber,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
            )
        }
    }
}

@Composable
fun ExerciseRow(e: Exercise, subtitle: String, onClick: () -> Unit) {
    AppCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                StickFigure(e.anim, Modifier.fillMaxWidth().height(64.dp).padding(4.dp), animate = false)
            }
            Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                Text(e.ar, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                Text(e.en, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DifficultyDots(e.difficulty)
        }
    }
}

@Composable
fun DifficultyDots(level: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        for (i in 1..5) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(
                        if (i <= level) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
            )
        }
    }
}

@Composable
fun BarChart(values: List<Int>, labels: List<String>, modifier: Modifier = Modifier) {
    val maxV = max(1, values.maxOrNull() ?: 1)
    val primary = MaterialTheme.colorScheme.primary
    val track = MaterialTheme.colorScheme.surfaceVariant
    Row(
        modifier = modifier.fillMaxWidth().height(110.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        values.forEachIndexed { i, v ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$v", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(width = 22.dp, height = 70.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(track),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((70f * v / maxV).dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(primary)
                    )
                }
                Text(labels.getOrElse(i) { "" }, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

fun formatClock(seconds: Int): String {
    val s = seconds.coerceAtLeast(0)
    val m = s / 60
    val r = s % 60
    return "%d:%02d".format(m, r)
}
