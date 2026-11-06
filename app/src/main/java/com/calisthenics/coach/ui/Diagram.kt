package com.calisthenics.coach.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.Equip
import com.calisthenics.coach.data.Exercise
import com.calisthenics.coach.data.Muscle
import com.calisthenics.coach.data.Pose
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.min

private const val HEAD_R = 0.058f

private enum class Seg { TORSO, UARM, FARM, THIGH, SHIN, HIP }

private fun segsFor(m: Muscle): Set<Seg> = when (m) {
    Muscle.CHEST -> setOf(Seg.TORSO, Seg.UARM)
    Muscle.SHOULDERS, Muscle.TRICEPS, Muscle.BICEPS -> setOf(Seg.UARM)
    Muscle.FOREARMS -> setOf(Seg.FARM)
    Muscle.BACK, Muscle.LOWER_BACK, Muscle.ABS, Muscle.CORE -> setOf(Seg.TORSO)
    Muscle.QUADS, Muscle.HAMSTRINGS -> setOf(Seg.THIGH)
    Muscle.CALVES -> setOf(Seg.SHIN)
    Muscle.GLUTES, Muscle.HIPS -> setOf(Seg.THIGH, Seg.HIP)
    Muscle.FULL -> setOf(Seg.TORSO, Seg.UARM, Seg.FARM, Seg.THIGH, Seg.SHIN)
}

private fun extents(pose: Pose): Pair<Float, Float> {
    var minX = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    var minY = Float.MAX_VALUE
    var maxY = -Float.MAX_VALUE
    for (i in 0 until 13) {
        val x = pose.p[i * 2]
        val y = pose.p[i * 2 + 1]
        minX = min(minX, x)
        maxX = max(maxX, x)
        minY = min(minY, y)
        maxY = max(maxY, y)
    }
    minY = min(minY, pose.p[1] - HEAD_R)
    val width = maxX - minX + 0.07f
    val height = maxY + 0.015f - minY + pose.lift
    return width to height
}

/** One shared scale for both frames of an exercise so the body never changes size between them. */
fun fitScale(a: Pose, b: Pose): Float {
    val (wa, ha) = extents(a)
    val (wb, hb) = extents(b)
    return minOf(0.80f / max(ha, hb), 0.88f / max(wa, wb), 0.90f)
}

@Composable
fun PoseCanvas(pose: Pose, scale: Float, muscle: Muscle, bandAt: Int?, modifier: Modifier = Modifier) {
    val body = MaterialTheme.colorScheme.onSurfaceVariant
    val far = body.copy(alpha = 0.50f)
    val accent = MaterialTheme.colorScheme.primary
    val farAccent = accent.copy(alpha = 0.55f)
    val floorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val hl = segsFor(muscle)

    Canvas(modifier = modifier) {
        val s = size.minDimension
        var minX = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (i in 0 until 13) {
            minX = min(minX, pose.p[i * 2])
            maxX = max(maxX, pose.p[i * 2])
            maxY = max(maxY, pose.p[i * 2 + 1])
        }
        val cx = (minX + maxX) / 2f
        fun pt(i: Int) = Offset(
            s * (0.5f + (pose.p[i * 2] - cx) * scale),
            s * (0.90f + (pose.p[i * 2 + 1] - maxY) * scale - pose.lift * scale)
        )

        fun seg(a: Int, b: Int, w: Float, part: Seg, isFar: Boolean) {
            val on = part in hl
            val c = if (isFar) (if (on) farAccent else far) else (if (on) accent else body)
            drawLine(c, pt(a), pt(b), strokeWidth = w * s, cap = StrokeCap.Round)
        }

        drawLine(floorColor, Offset(0.04f * s, 0.905f * s), Offset(0.96f * s, 0.905f * s), strokeWidth = 0.012f * s, cap = StrokeCap.Round)

        // far limbs first (behind the body)
        seg(1, 8, 0.040f, Seg.UARM, true)
        seg(8, 9, 0.034f, Seg.FARM, true)
        seg(4, 10, 0.060f, Seg.THIGH, true)
        seg(10, 11, 0.048f, Seg.SHIN, true)
        seg(11, 12, 0.030f, Seg.SHIN, true)
        // torso and near leg
        seg(4, 1, 0.085f, Seg.TORSO, false)
        if (Seg.HIP in hl) drawCircle(accent, radius = 0.05f * s, center = pt(4))
        seg(4, 5, 0.066f, Seg.THIGH, false)
        seg(5, 6, 0.052f, Seg.SHIN, false)
        seg(6, 7, 0.032f, Seg.SHIN, false)
        // head and near arm
        drawLine(body, pt(1), pt(0), strokeWidth = 0.03f * s, cap = StrokeCap.Round)
        drawCircle(body, radius = HEAD_R * s, center = pt(0))
        seg(1, 2, 0.046f, Seg.UARM, false)
        seg(2, 3, 0.038f, Seg.FARM, false)

        bandAt?.let { drawBand(it, ::pt, s) }
    }
}

private val BandColor = Color(0xFFF59E0B)

private fun DrawScope.drawBand(joint: Int, pt: (Int) -> Offset, s: Float) {
    when (joint) {
        Pose.KNEE -> drawLine(BandColor, pt(5), pt(10), strokeWidth = 0.02f * s, cap = StrokeCap.Round)
        Pose.FOOT -> drawLine(BandColor, pt(6), pt(3), strokeWidth = 0.014f * s, cap = StrokeCap.Round)
        Pose.HAND -> drawLine(BandColor, pt(3), pt(9), strokeWidth = 0.02f * s, cap = StrokeCap.Round)
        else -> drawCircle(BandColor, radius = 0.07f * s, center = pt(1), style = Stroke(width = 0.016f * s))
    }
}

@Composable
private fun Panel(caption: String, pose: Pose, e: Exercise, scale: Float, active: Boolean, modifier: Modifier) {
    val border = if (active) MaterialTheme.colorScheme.primary else Color.Transparent
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(BorderStroke(2.dp, border), RoundedCornerShape(14.dp))
        ) {
            PoseCanvas(pose, scale, e.muscle, e.anim.bandAt, Modifier.fillMaxWidth().aspectRatio(1f))
        }
        Text(
            caption,
            fontSize = 12.sp,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

/** Start and end positions side by side, with the target muscle highlighted in green. */
@Composable
fun ExerciseIllustration(e: Exercise, modifier: Modifier = Modifier, animate: Boolean = true) {
    val a = e.anim.a
    val b = e.anim.b
    val hold = a === b || a.p.contentEquals(b.p)
    val scale = remember(e.id) { fitScale(a, b) }
    var phase by remember(e.id) { mutableIntStateOf(0) }
    if (animate && !hold) {
        LaunchedEffect(e.id) {
            while (true) {
                delay(1400)
                phase = 1 - phase
            }
        }
    }
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        if (hold) {
            Panel("ثبّت هذا الوضع", a, e, scale, false, Modifier.fillMaxWidth(0.6f).align(Alignment.CenterHorizontally))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Panel("١ البداية", a, e, scale, animate && phase == 0, Modifier.weight(1f))
                Text("←", fontSize = 20.sp, color = MaterialTheme.colorScheme.outline)
                Panel("٢ النهاية", b, e, scale, animate && phase == 1, Modifier.weight(1f))
            }
        }
        val note = buildString {
            append("منظر جانبي توضيحي. الأخضر = العضلة المستهدفة")
            if (e.equip == Equip.BAND) append("، والخط البرتقالي = حلقة المقاومة")
        }
        Text(
            note,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
        )
    }
}

@Composable
fun ExerciseThumb(e: Exercise, modifier: Modifier = Modifier) {
    val scale = remember(e.id) { fitScale(e.anim.a, e.anim.b) }
    Box(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        PoseCanvas(e.anim.a, scale, e.muscle, null, Modifier.fillMaxWidth().aspectRatio(1f))
    }
}
