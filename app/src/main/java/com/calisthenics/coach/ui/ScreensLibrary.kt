package com.calisthenics.coach.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.Equip
import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.data.Info
import com.calisthenics.coach.data.Muscle
import com.calisthenics.coach.data.Skills
import com.calisthenics.coach.data.SkillStep
import com.calisthenics.coach.logic.AppState
import com.calisthenics.coach.logic.AppViewModel

// ============================================================ library

@Composable
fun LibraryScreen(onOpen: (String) -> Unit) {
    var filter by remember { mutableStateOf<Muscle?>(null) }
    var showLimits by remember { mutableStateOf(false) }
    val muscles = Muscle.entries.filter { it != Muscle.FULL }
    val list = ExerciseData.all
        .filter { filter == null || it.muscle == filter }
        .sortedWith(compareBy({ it.kind.ordinal }, { it.difficulty }))

    ScreenScaffold("مكتبة التمارين") {
        OutlinedButton(onClick = { showLimits = true }, modifier = Modifier.fillMaxWidth()) {
            Text("ما حدود معداتك؟ (حلقات قصيرة فقط)")
        }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Chip("الكل", filter == null, { filter = null })
            muscles.forEach { m -> Chip(m.ar, filter == m, { filter = m }) }
        }
        Text("${list.size} تمرين", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        list.forEach { e ->
            val tag = when (e.kind) {
                com.calisthenics.coach.data.Kind.WARMUP -> "إحماء"
                com.calisthenics.coach.data.Kind.COOLDOWN -> "تبريد"
                else -> if (e.equip == Equip.BAND) "حلقة مقاومة" else "وزن الجسم"
            }
            ExerciseRow(e, "${e.muscle.ar} • $tag") { onOpen(e.id) }
        }
    }

    if (showLimits) {
        AlertDialog(
            onDismissRequest = { showLimits = false },
            confirmButton = { TextButton(onClick = { showLimits = false }) { Text("فهمت") } },
            title = { Text("حدود المعدات") },
            text = {
                Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Info.equipmentLimits.forEach { sec ->
                        Text(sec.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        BulletList(sec.points)
                    }
                }
            }
        )
    }
}

@Composable
fun ExerciseDetailScreen(id: String, state: AppState, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val e = ExerciseData.get(id)
    if (e == null) {
        ScreenScaffold("تمرين", onBack) { Text("هذا التمرين غير موجود.") }
        return
    }
    val target = state.targets[e.id] ?: e.amount
    val record = state.records[e.id]
    val unit = if (e.timed) "ثانية" else "تكرار"

    ScreenScaffold(e.ar, onBack) {
        Text(e.en, color = MaterialTheme.colorScheme.outline)
        ExerciseIllustration(e, Modifier.fillMaxWidth())
        VideoButton(e, Modifier.fillMaxWidth())
        Text(
            "الرسم توضيحي مبسط لمسار الحركة، اتبع الشرح المكتوب للتفاصيل.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(e.muscle.ar)
            Pill(e.equip.ar, color = if (e.equip == Equip.BAND) Amber else MaterialTheme.colorScheme.primary)
            Pill("المرحلة ${e.minStage}+")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("الصعوبة: ", fontWeight = FontWeight.Bold)
            DifficultyDots(e.difficulty)
        }
        AppCard {
            Text("الجرعة المقترحة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer8()
            val side = if (e.perSide) " لكل جانب" else ""
            Text("${e.sets} مجموعات × $target $unit$side")
            Text("الراحة بين المجموعات: ${e.rest} ثانية")
            if (e.also.isNotBlank()) Text("عضلات مساعدة: ${e.also}")
            if (record != null && record > 0) Text("أفضل رقم لك: $record $unit", color = MaterialTheme.colorScheme.primary)
        }
        AppCard {
            LabelTitle("وضع البداية")
            Text(e.start)
            LabelTitle("خطوات التنفيذ")
            e.steps.forEachIndexed { i, step ->
                Row(Modifier.padding(vertical = 3.dp)) {
                    Text("${i + 1}.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(end = 8.dp))
                    Text(step)
                }
            }
            LabelTitle("التنفس")
            Text(e.breathing)
            LabelTitle("أخطاء شائعة")
            BulletList(e.mistakes, "✗")
        }
        WarningBox(e.safety)
        if (e.easier != null || e.harder != null) {
            AppCard {
                Text("النسخ الأسهل والأصعب", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                e.easier?.let { eid ->
                    ExerciseData.get(eid)?.let { x ->
                        TextButton(onClick = { onOpen(eid) }) { Text("⬇ أسهل: ${x.ar}") }
                    }
                }
                e.harder?.let { hid ->
                    ExerciseData.get(hid)?.let { x ->
                        TextButton(onClick = { onOpen(hid) }) { Text("⬆ أصعب: ${x.ar}") }
                    }
                }
            }
        }
    }
}

@Composable
private fun LabelTitle(text: String) {
    Text(
        text,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 10.dp, bottom = 2.dp)
    )
}

// ============================================================ skills

private val levelNames = listOf("لم أبدأ", "مبتدئ", "متوسط", "متقدم")

@Composable
fun SkillsScreen(vm: AppViewModel, onOpen: (String) -> Unit) {
    val home = Skills.all.filter { it.homeSafe }
    val gear = Skills.all.filter { !it.homeSafe }
    ScreenScaffold("المهارات") {
        WarningBox("المهارات المتقدمة تتطلب تدرجًا بطيئًا. لا تحاول مهارة قبل أن تجتاز اختبارات جاهزيتها، ولا تستخدم أي شيء مرتجل للتعلق أو الدعم.")
        SectionTitle("يمكن تدريبها في المنزل الآن")
        home.forEach { sk ->
            val lvl = vm.state.skillProgress[sk.id] ?: 0
            AppCard(onClick = { onOpen(sk.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(sk.ar, fontWeight = FontWeight.Bold)
                        Text(sk.en, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Pill(levelNames[lvl.coerceIn(0, 3)])
                }
            }
        }
        SectionTitle("تحتاج معدات أو إشرافًا (غير متاحة الآن)")
        gear.forEach { sk ->
            AppCard(onClick = { onOpen(sk.id) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(sk.ar, fontWeight = FontWeight.Bold)
                        Text(sk.en, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Pill("غير متاحة", color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun SkillDetailScreen(id: String, vm: AppViewModel, onBack: () -> Unit, onOpen: (String) -> Unit) {
    val sk = Skills.get(id)
    if (sk == null) {
        ScreenScaffold("مهارة", onBack) { Text("هذه المهارة غير موجودة.") }
        return
    }
    val s = vm.state
    val lvl = s.skillProgress[sk.id] ?: 0

    ScreenScaffold(sk.ar, onBack) {
        Text(sk.en, color = MaterialTheme.colorScheme.outline)
        if (sk.homeSafe) {
            Pill("يمكن تدريبها بأمان في المنزل بمعداتك الحالية")
        } else {
            Pill("غير متاحة بمعداتك الحالية", color = MaterialTheme.colorScheme.secondary)
        }
        WarningBox(sk.equipmentNote)

        AppCard {
            LabelTitle("المتطلبات المسبقة")
            BulletList(sk.prerequisites)
        }
        StepsCard("المرحلة المبتدئة", sk.beginner, onOpen)
        StepsCard("المرحلة المتوسطة", sk.intermediate, onOpen)
        StepsCard("المرحلة المتقدمة", sk.advanced, onOpen)

        AppCard {
            LabelTitle("اختبارات الجاهزية (بدون جهد أقصى)")
            sk.readinessTests.forEachIndexed { i, t ->
                val key = "${sk.id}|$i"
                Row(
                    Modifier.fillMaxWidth().clickable { vm.toggleSkillTest(key) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(checked = key in s.skillTests, onCheckedChange = { vm.toggleSkillTest(key) })
                    Text(t, modifier = Modifier.weight(1f))
                }
            }
            Text(
                "ضع علامة فقط إذا أنجزت الاختبار بتحكم وبدون ألم وبدون الوصول إلى أقصى جهدك.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        if (sk.homeSafe) {
            AppCard {
                LabelTitle("مستواي الحالي في هذه المهارة")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    levelNames.forEachIndexed { i, n -> Chip(n, lvl == i, { vm.setSkillLevel(sk.id, i) }) }
                }
            }
        }

        AppCard {
            LabelTitle("تحذيرات وحدود")
            BulletList(sk.warnings, "⚠")
        }
    }
}

@Composable
private fun StepsCard(title: String, steps: List<SkillStep>, onOpen: (String) -> Unit) {
    AppCard {
        LabelTitle(title)
        steps.forEach { st ->
            Column(Modifier.padding(vertical = 4.dp)) {
                Text(st.title, fontWeight = FontWeight.SemiBold)
                Text(st.detail, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                val ex = st.exerciseId
                if (ex != null && ExerciseData.get(ex) != null) {
                    TextButton(onClick = { onOpen(ex) }) { Text("عرض التمرين") }
                }
            }
        }
    }
}
