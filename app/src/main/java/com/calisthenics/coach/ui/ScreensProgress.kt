package com.calisthenics.coach.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.data.Kind
import com.calisthenics.coach.data.Skills
import com.calisthenics.coach.data.Stages
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.logic.Dates
import com.calisthenics.coach.logic.Progress
import java.util.Calendar

private val dayInitials = listOf("س", "ح", "ن", "ث", "ر", "خ", "ج")
private val skillLevelNames = listOf("لم أبدأ", "مبتدئ", "متوسط", "متقدم")

@Composable
fun ProgressScreen(vm: AppViewModel, onOpen: (Route) -> Unit) {
    val s = vm.state
    val stage = Stages.get(s.stage)
    var showPlank by remember { mutableStateOf(false) }
    var plankText by remember { mutableStateOf("") }

    ScreenScaffold("التقدم") {
        AppCard(onClick = { onOpen(Route.Stages) }) {
            Text("المستوى الحالي", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text("المرحلة ${stage.number}: ${stage.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer8()
            Text("اضغط لمعرفة شروط التقدم للمرحلة التالية", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox("${s.workouts.size}", "تمرين مكتمل", Modifier.weight(1f))
            StatBox("${Progress.totalMinutes(s)}", "دقيقة تدريب", Modifier.weight(1f))
            StatBox("${Progress.streakWeeks(s)}", "أسابيع التزام", Modifier.weight(1f))
        }

        SectionTitle("الالتزام الأسبوعي (آخر 8 أسابيع)")
        AppCard {
            val weeks = Progress.weeklyCounts(s, 8)
            BarChart(weeks.map { it.second }, weeks.map { Dates.formatShort(it.first) })
        }

        SectionTitle("تقويم التدريب")
        CalendarCard(vm)

        SectionTitle("مدة البلانك")
        AppCard {
            val best = Progress.bestPlank(s)
            Text("أفضل مدة: $best ثانية", fontWeight = FontWeight.Bold)
            Spacer8()
            val last = s.plankHistory.takeLast(8)
            if (last.isEmpty()) {
                Text("لا توجد قياسات بعد. سجّل مدة بلانك أو أضف تمرين البلانك في جلساتك.", fontSize = 13.sp)
            } else {
                BarChart(last.map { it.seconds }, last.map { Dates.formatShort(it.dateKey) })
            }
            Spacer8()
            OutlinedButton(onClick = { showPlank = true }) { Text("تسجيل قياس بلانك") }
        }

        SectionTitle("الأرقام القياسية")
        AppCard {
            val recs = s.records.filter { it.value > 0 && ExerciseData.get(it.key) != null }
            if (recs.isEmpty()) {
                Text("ستظهر أرقامك القياسية هنا بعد أول جلسة.", fontSize = 13.sp)
            } else {
                recs.entries.sortedBy { ExerciseData.require(it.key).ar }.forEach { (id, v) ->
                    val e = ExerciseData.require(id)
                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                        Text(e.ar, modifier = Modifier.weight(1f))
                        Text(if (e.timed) "$v ثانية" else "$v تكرار", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        SectionTitle("تقدم المهارات")
        AppCard {
            Skills.all.filter { it.homeSafe }.forEach { sk ->
                val lvl = (s.skillProgress[sk.id] ?: 0).coerceIn(0, 3)
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(sk.ar, modifier = Modifier.weight(1f))
                    Pill(skillLevelNames[lvl])
                }
            }
            Text(
                "المهارات التي تحتاج معدات غير متاحة وغير مدرجة هنا.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }

        SectionTitle("إنجازات")
        AppCard {
            Progress.milestones(s).forEach { m ->
                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (m.achieved) "🏅" else "⚪", modifier = Modifier.padding(end = 8.dp))
                    Text(
                        m.title,
                        color = if (m.achieved) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                    )
                }
            }
        }

        SectionTitle("سجل التمارين")
        if (s.workouts.isEmpty()) {
            AppCard { Text("لم تكمل أي جلسة بعد.") }
        } else {
            s.workouts.sortedByDescending { it.id }.take(10).forEach { w ->
                AppCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(w.dateKey, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${w.durationSec / 60} دقيقة", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer8()
                    w.results.filter { r ->
                        ExerciseData.get(r.id)?.kind == Kind.STRENGTH && r.setsDone > 0
                    }.forEach { r ->
                        val e = ExerciseData.require(r.id)
                        val unit = if (e.timed) "ث" else "تكرار"
                        Text("• ${e.ar}: ${r.setsDone}/${r.setsPlanned} مجموعات، الأفضل ${r.best} $unit", fontSize = 13.sp)
                    }
                    if (w.pain || w.fatigue) {
                        Spacer8()
                        Text("سُجّل ألم أو إرهاق في هذه الجلسة", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }

    if (showPlank) {
        AlertDialog(
            onDismissRequest = { showPlank = false },
            title = { Text("قياس البلانك") },
            text = {
                Column {
                    Text("اكتب المدة بالثواني التي ثبتّها بشكل نظيف (لا تصل لأقصى جهدك).")
                    OutlinedTextField(
                        value = plankText,
                        onValueChange = { v -> plankText = v.filter { it.isDigit() }.take(3) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        label = { Text("ثانية") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.addPlank(plankText.toIntOrNull() ?: 0)
                    plankText = ""
                    showPlank = false
                }) { Text("حفظ") }
            },
            dismissButton = { TextButton(onClick = { showPlank = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun CalendarCard(vm: AppViewModel) {
    val s = vm.state
    var monthOffset by remember { mutableIntStateOf(0) }
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        add(Calendar.MONTH, monthOffset)
    }
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH)
    val offset = Dates.daySat0(cal)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val done = Progress.workoutDates(s)
    val todayKey = Dates.todayKey()

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { monthOffset-- }) { Text("‹") }
            Text(
                "${Dates.monthName(month)} $year",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = { if (monthOffset < 0) monthOffset++ }, enabled = monthOffset < 0) { Text("›") }
        }
        Row(Modifier.fillMaxWidth()) {
            dayInitials.forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
        val totalCells = ((offset + daysInMonth + 6) / 7) * 7
        for (rowStart in 0 until totalCells step 7) {
            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                for (c in 0 until 7) {
                    val dayNum = rowStart + c - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (dayNum in 1..daysInMonth) {
                            val key = "%04d-%02d-%02d".format(year, month + 1, dayNum)
                            val trained = key in done
                            val isToday = key == todayKey
                            val isTrainingDay = c in s.trainingDays
                            val bg = when {
                                trained -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isTrainingDay) 0.9f else 0.35f)
                            }
                            Box(
                                Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .let { m -> if (isToday) m.border(BorderStroke(2.dp, MaterialTheme.colorScheme.secondary), CircleShape) else m },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$dayNum",
                                    fontSize = 12.sp,
                                    color = if (trained) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer8()
        Text("الأخضر: يوم تدريب مكتمل • الرمادي الغامق: يوم تدريب مخطط • الباهت: يوم راحة", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
    }
}

// ============================================================ stages

@Composable
fun StagesScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.state
    val stage = Stages.get(s.stage)
    val items = Progress.readiness(s)
    var confirm by remember { mutableStateOf(false) }

    ScreenScaffold("المراحل والتقدم", onBack) {
        AppCard {
            Text("المرحلة الحالية: ${stage.number} - ${stage.name}", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer8()
            Text(stage.summary)
            Spacer8()
            Text("المجموعات: ${stage.setsRange}", fontSize = 13.sp)
            Text("الراحة: ${stage.rest}", fontSize = 13.sp)
        }

        if (s.stage < Stages.all.size) {
            AppCard {
                Text("شروط الانتقال للمرحلة التالية", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("لا يوجد موعد ثابت. يعتمد الانتقال على التقنية والالتزام والتعافي والقدرة الفعلية.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Spacer8()
                items.forEach { r ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                        if (r.manual) {
                            Checkbox(checked = r.done, onCheckedChange = { vm.toggleStageCheck(r.key) })
                        } else {
                            Text(if (r.done) "✅" else "⬜", modifier = Modifier.padding(horizontal = 12.dp))
                        }
                        Text(r.label, modifier = Modifier.weight(1f), fontSize = 14.sp)
                    }
                }
                Spacer8()
                Button(
                    onClick = { confirm = true },
                    enabled = Progress.canAdvance(s),
                    modifier = Modifier.fillMaxWidth()
                ) { Text("الانتقال للمرحلة ${s.stage + 1}") }
                if (!Progress.canAdvance(s)) {
                    Text("الزر يتفعل عندما تتحقق كل الشروط.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            AppCard {
                Text("أنت في المرحلة الأخيرة. الهدف الآن إتقان المهارات بأمان وليس الإسراع.")
            }
        }

        AppCard {
            Text("تحذيرات هذه المرحلة", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer8()
            BulletList(stage.safety, "⚠")
        }

        SectionTitle("كل المراحل")
        Stages.all.forEach { st ->
            AppCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${st.number}. ${st.name}", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    if (st.number == s.stage) Pill("الحالية")
                }
                Spacer8()
                Text(st.summary, fontSize = 13.sp)
                Spacer8()
                Text("ترتيب الجلسات", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(st.rotation.joinToString(" ← ") { it.ar }, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer8()
                Text("معايير التقدم", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                BulletList(st.progression)
            }
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("الانتقال للمرحلة التالية") },
            text = { Text("ستصبح التمارين أصعب قليلًا. تأكد أنك ترتاح جيدًا وتؤدي الحركات بتقنية نظيفة. هل تريد المتابعة؟") },
            confirmButton = {
                TextButton(onClick = {
                    vm.advanceStage()
                    confirm = false
                }) { Text("نعم، انتقل") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("ليس الآن") } }
        )
    }
}
