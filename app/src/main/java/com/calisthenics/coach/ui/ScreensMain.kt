package com.calisthenics.coach.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.data.Info
import com.calisthenics.coach.data.Kind
import com.calisthenics.coach.data.Stages
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.logic.DAY_NAMES_SAT0
import com.calisthenics.coach.logic.Dates
import com.calisthenics.coach.logic.Generator
import com.calisthenics.coach.logic.Progress
import kotlin.math.roundToInt

// ============================================================ onboarding

@Composable
fun OnboardingScreen(vm: AppViewModel) {
    var minutes by remember { mutableFloatStateOf(45f) }
    Column(
        Modifier
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("مرحبًا بك في مدرب الكاليسثينكس 💪", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "برنامج تدريب منزلي بوزن الجسم وحلقات المقاومة القصيرة، يبدأ من مستوى المبتدئ ويتقدم حسب قدرتك الفعلية وليس حسب عدد الأسابيع.",
            style = MaterialTheme.typography.bodyLarge
        )
        AppCard {
            Text("كيف يعمل التطبيق", fontWeight = FontWeight.Bold)
            Spacer8()
            BulletList(
                listOf(
                    "يعمل بالكامل بدون إنترنت، وبياناتك محفوظة على هاتفك فقط.",
                    "لا حسابات ولا إعلانات ولا اشتراكات.",
                    "التطبيق لا يزيد الصعوبة إذا سجلت ألمًا أو إرهاقًا.",
                    "لا نصف تمارين تحتاج عقلة أو معدات غير موجودة لديك."
                )
            )
        }
        AppCard {
            Text("السلامة أولًا", fontWeight = FontWeight.Bold)
            Spacer8()
            BulletList(Info.safetyRules)
            Spacer8()
            Text(
                "إن كانت لديك حالة صحية أو تتناول دواءً، فتحدث مع أحد والديك وطبيبك قبل البدء. هذا التطبيق للتثقيف العام وليس نصيحة طبية.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        AppCard {
            Text("مدة التمرين المتاحة: ${minutes.roundToInt()} دقيقة", fontWeight = FontWeight.Bold)
            Slider(value = minutes, onValueChange = { minutes = it }, valueRange = 20f..60f, steps = 7)
            Text("يمكنك تغييرها لاحقًا من الإعدادات.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }
        Button(
            onClick = { vm.finishOnboarding((minutes / 5f).roundToInt() * 5) },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("ابدأ التدريب", fontSize = 16.sp) }
    }
}

@Composable
fun Spacer8() {
    androidx.compose.foundation.layout.Spacer(Modifier.height(8.dp))
}

// ============================================================ home

@Composable
fun HomeScreen(vm: AppViewModel, onTrain: () -> Unit, onOpen: (Route) -> Unit) {
    val s = vm.state
    val stage = Stages.get(s.stage)
    val today = Dates.todaySat0()
    val isTrainingDay = today in s.trainingDays
    val doneToday = s.workouts.any { it.dateKey == Dates.todayKey() }
    val weekStart = Dates.weekStartKey(java.util.Calendar.getInstance())
    val thisWeek = Progress.workoutsInWeek(s, weekStart)

    ScreenScaffold("الرئيسية") {
        AppCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("مستواك الحالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    Text("المرحلة ${stage.number}: ${stage.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Pill("${stage.trainingDays} أيام أسبوعيًا")
            }
            Spacer8()
            Text(stage.summary, style = MaterialTheme.typography.bodyMedium)
            Spacer8()
            OutlinedButton(onClick = { onOpen(Route.Stages) }) { Text("تفاصيل المراحل وشروط التقدم") }
        }

        AppCard {
            Text("اليوم: ${DAY_NAMES_SAT0[today]}", fontWeight = FontWeight.Bold)
            Spacer8()
            val msg = when {
                doneToday -> "أنهيت تمرين اليوم، أحسنت! اترك جسمك يتعافى."
                isTrainingDay -> "اليوم يوم تدريب حسب جدولك."
                else -> "اليوم يوم راحة حسب جدولك. الراحة جزء من التقدم، ويمكنك المشي الخفيف أو الإطالة."
            }
            Text(msg)
            Spacer8()
            Button(onClick = onTrain, modifier = Modifier.fillMaxWidth()) {
                Text(if (doneToday || !isTrainingDay) "عرض تمرين اليوم (اختياري)" else "ابدأ تمرين اليوم")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox("${s.workouts.size}", "تمرين مكتمل", Modifier.weight(1f))
            StatBox("$thisWeek / ${s.trainingDays.size}", "هذا الأسبوع", Modifier.weight(1f))
            StatBox("${Progress.streakWeeks(s)}", "أسابيع التزام", Modifier.weight(1f))
        }

        SectionTitle("جدول الأسبوع")
        AppCard {
            stage.schedule.forEachIndexed { i, label ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        DAY_NAMES_SAT0[i],
                        modifier = Modifier.weight(0.35f),
                        fontWeight = if (i == today) FontWeight.Bold else FontWeight.Normal,
                        color = if (i == today) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        if (i in s.trainingDays) "تدريب" else "راحة",
                        modifier = Modifier.weight(0.65f),
                        color = if (i in s.trainingDays) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer8()
            Text(
                "أيام التدريب تتبع اختيارك في الإعدادات، ونوع كل جلسة يختاره المولّد تلقائيًا بالتناوب.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { onOpen(Route.Nutrition) }, modifier = Modifier.weight(1f)) { Text("التغذية والتعافي") }
            OutlinedButton(onClick = { onOpen(Route.Settings) }, modifier = Modifier.weight(1f)) { Text("الإعدادات") }
        }
    }
}

// ============================================================ train

@Composable
fun TrainScreen(vm: AppViewModel, onStart: () -> Unit, onOpen: (Route) -> Unit) {
    LaunchedEffect(Unit) { vm.ensurePlan() }
    val s = vm.state
    var replaceIndex by remember { mutableStateOf<Int?>(null) }
    val plan = s.plan
    val minutes = Generator.estimateMinutes(plan)

    ScreenScaffold("تمرين اليوم") {
        AppCard {
            Text("كيف تشعر اليوم؟", fontWeight = FontWeight.Bold)
            Spacer8()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("بخير", s.planReadiness == 0, { vm.regenerate(0) })
                Chip("متعب", s.planReadiness == 1, { vm.regenerate(1) })
                Chip("عندي ألم", s.planReadiness == 2, { vm.regenerate(2) })
            }
            Spacer8()
            val note = when {
                s.planReadiness == 2 -> "بسبب الألم تم استبدال التمرين بجلسة حركة خفيفة وإطالة فقط. إن استمر الألم فأخبر شخصًا بالغًا أو استشر طبيبًا."
                s.planReadiness == 1 || Generator.recoveryFlag(s) -> "تم تخفيف الحجم لأن التعافي ليس في أفضل حالاته، ولن تزداد الصعوبة الآن."
                else -> "الخطة مبنية على مستواك الحالي ومدة جلستك (${s.sessionMinutes} دقيقة)."
            }
            Text(note, style = MaterialTheme.typography.bodyMedium)
        }

        if (plan.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill("المدة التقريبية: $minutes دقيقة")
                Pill("${plan.count { ExerciseData.get(it.exerciseId)?.kind == Kind.STRENGTH }} تمارين قوة")
            }
        }

        var lastKind: Kind? = null
        plan.forEachIndexed { i, item ->
            val e = ExerciseData.get(item.exerciseId) ?: return@forEachIndexed
            if (e.kind != lastKind) {
                lastKind = e.kind
                SectionTitle(
                    when (e.kind) {
                        Kind.WARMUP -> "الإحماء"
                        Kind.STRENGTH -> "التمرين الأساسي"
                        Kind.COOLDOWN -> "التبريد"
                    }
                )
            }
            val perSide = if (e.perSide) " لكل جانب" else ""
            val label = if (e.timed) "${item.amount} ثانية" else "${item.amount} تكرار"
            ExerciseRow(e, "${item.sets} × $label$perSide • راحة ${item.rest} ث") { onOpen(Route.ExerciseDetail(e.id)) }
            if (e.kind == Kind.STRENGTH) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { replaceIndex = i }) { Text("استبدال التمرين") }
                    if (e.easier != null) {
                        TextButton(onClick = { vm.replaceExercise(i, e.easier) }) { Text("أريد نسخة أسهل") }
                    }
                }
            }
        }

        Button(
            onClick = onStart,
            enabled = plan.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().height(54.dp)
        ) { Text("ابدأ الجلسة", fontSize = 17.sp) }
    }

    val idx = replaceIndex
    if (idx != null && idx in plan.indices) {
        val current = ExerciseData.get(plan[idx].exerciseId)
        if (current != null) {
            val alts = Generator.alternatives(s, current, plan)
            AlertDialog(
                onDismissRequest = { replaceIndex = null },
                confirmButton = { TextButton(onClick = { replaceIndex = null }) { Text("إغلاق") } },
                title = { Text("بدائل لـ ${current.ar}") },
                text = {
                    if (alts.isEmpty()) {
                        Text("لا توجد بدائل مناسبة لمستواك لنفس العضلة حاليًا.")
                    } else {
                        Column(
                            Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            alts.forEach { a ->
                                TextButton(
                                    onClick = {
                                        vm.replaceExercise(idx, a.id)
                                        replaceIndex = null
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) { Text("${a.ar} (${a.en})") }
                            }
                        }
                    }
                }
            )
        }
    }
}
