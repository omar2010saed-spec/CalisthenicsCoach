package com.calisthenics.coach.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.logic.Feedback
import com.calisthenics.coach.logic.Phase
import com.calisthenics.coach.logic.PlanItem
import com.calisthenics.coach.logic.WorkoutSession
import kotlinx.coroutines.delay

@Composable
fun WorkoutScreen(vm: AppViewModel, session: WorkoutSession, onExit: () -> Unit) {
    val context = LocalContext.current
    val view = LocalView.current
    val feedback = remember { Feedback(context) }
    var showStop by remember { mutableStateOf(false) }
    var painPreset by remember { mutableStateOf(false) }

    DisposableEffect(Unit) { onDispose { feedback.release() } }
    DisposableEffect(view) {
        view.keepScreenOn = true
        onDispose { view.keepScreenOn = false }
    }
    LaunchedEffect(session) {
        while (true) {
            delay(1000)
            session.tick()
        }
    }
    LaunchedEffect(session.signal) {
        val sig = session.signal
        if (sig == 1) feedback.tick(vm.state.sound, vm.state.vibration)
        if (sig == 2) feedback.done(vm.state.sound, vm.state.vibration)
        if (sig != 0) session.consumeSignal()
    }
    BackHandler(enabled = session.phase != Phase.FINISHED) { showStop = true }

    if (session.phase == Phase.FINISHED) {
        FinishedView(vm, session, painPreset, onExit)
        return
    }

    val e = session.exercise
    val item = session.item
    val unit = if (e.timed) "ثانية" else "تكرار"
    val total = session.items.size

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("التمرين ${session.index + 1} من $total", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                Text("زمن الجلسة ${formatClock(session.elapsed)}", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(onClick = { session.togglePause() }) {
                Text(if (session.paused) "استئناف ▶" else "إيقاف مؤقت ⏸")
            }
            TextButton(onClick = { showStop = true }) { Text("إنهاء", color = MaterialTheme.colorScheme.error) }
        }
        LinearProgressIndicator(
            progress = { (session.index + 1f) / total },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        )

        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ExerciseIllustration(e, Modifier.fillMaxWidth(), animate = !session.paused)
            VideoButton(e, Modifier.fillMaxWidth())
            Text(e.ar, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(e.en, fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)

            if (session.paused) {
                AppCard {
                    Text("الجلسة متوقفة مؤقتًا", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("اضغط استئناف عندما تكون جاهزًا.")
                }
            } else {
                when (session.phase) {
                    Phase.REST -> RestPanel(session)
                    Phase.WORK -> WorkPanel(session)
                    else -> ReadyPanel(session, item, unit)
                }
            }

            AppCard {
                Text("الشرح", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer8()
                Text(e.start, fontSize = 13.sp)
                Spacer8()
                e.steps.forEachIndexed { i, st -> Text("${i + 1}. $st", fontSize = 13.sp, modifier = Modifier.padding(vertical = 2.dp)) }
                Spacer8()
                Text("التنفس: ${e.breathing}", fontSize = 13.sp)
            }
            WarningBox(e.safety)
            OutlinedButton(
                onClick = {
                    painPreset = true
                    session.finishNow()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("أشعر بألم — أوقف الجلسة", color = MaterialTheme.colorScheme.error) }
        }

        Row(
            Modifier.fillMaxWidth().padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = { session.previous() }, enabled = session.index > 0, modifier = Modifier.weight(1f)) {
                Text("السابق")
            }
            OutlinedButton(onClick = { session.skipExercise() }, modifier = Modifier.weight(1f)) { Text("تخطي") }
            Button(onClick = { session.next() }, modifier = Modifier.weight(1f)) {
                Text(if (session.isLast) "إنهاء" else "التالي")
            }
        }
    }

    if (showStop) {
        AlertDialog(
            onDismissRequest = { showStop = false },
            title = { Text("إنهاء الجلسة؟") },
            text = { Text("يمكنك إيقاف الجلسة بأمان في أي وقت. اختر ما تريد فعله بما أنجزته.") },
            confirmButton = {
                if (session.anythingDone()) {
                    TextButton(onClick = {
                        showStop = false
                        session.finishNow()
                    }) { Text("إنهاء وحفظ ما أنجزت") }
                } else {
                    TextButton(onClick = {
                        showStop = false
                        vm.discardSession()
                        onExit()
                    }) { Text("إنهاء") }
                }
            },
            dismissButton = {
                Row {
                    if (session.anythingDone()) {
                        TextButton(onClick = {
                            showStop = false
                            vm.discardSession()
                            onExit()
                        }) { Text("إنهاء بدون حفظ") }
                    }
                    TextButton(onClick = { showStop = false }) { Text("متابعة") }
                }
            }
        )
    }
}

@Composable
private fun ReadyPanel(session: WorkoutSession, item: PlanItem, unit: String) {
    val e = session.exercise
    AppCard {
        Text(
            "المجموعة ${session.currentSet} من ${item.sets}" + if (e.perSide) " (لكل جانب)" else "",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text("الهدف: ${item.amount} $unit • راحة ${item.rest} ث", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer8()
        if (e.timed) {
            Button(onClick = { session.startTimed() }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("ابدأ المؤقت", fontSize = 16.sp)
            }
        } else {
            Text("كم تكرارًا أنجزت بشكل نظيف؟", fontSize = 13.sp)
            Row(
                Modifier.fillMaxWidth().padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { if (session.reps > 0) session.reps = session.reps - 1 }) { Text("−") }
                Text(
                    "${session.reps}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                OutlinedButton(onClick = { if (session.reps < 200) session.reps = session.reps + 1 }) { Text("+") }
            }
            Button(onClick = { session.completeSet(session.reps) }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text("أنهيت المجموعة ✓", fontSize = 16.sp)
            }
        }
        val easier = e.easier?.let { ExerciseData.get(it) }
        if (easier != null && session.setsDone[session.index] == 0) {
            TextButton(onClick = {
                session.replaceCurrent(PlanItem(easier.id, item.sets, easier.amount, easier.rest))
            }) { Text("التمرين صعب؟ استبدله بـ: ${easier.ar}") }
        }
    }
}

@Composable
private fun WorkPanel(session: WorkoutSession) {
    val e = session.exercise
    AppCard {
        Text(
            "المجموعة ${session.currentSet} من ${session.item.sets}" + if (e.perSide) " — الجانب ${if (session.side == 1) "الأول" else "الثاني"}" else "",
            fontWeight = FontWeight.Bold
        )
        Text(
            formatClock(session.workLeft),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text("تنفّس بانتظام وحافظ على الشكل.", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        TextButton(onClick = { session.completeSet(session.item.amount - session.workLeft) }) { Text("أنهِ المجموعة الآن") }
    }
}

@Composable
private fun RestPanel(session: WorkoutSession) {
    val nextName = if (session.setsDone[session.index] >= session.item.sets && !session.isLast) {
        ExerciseData.get(session.items[session.index + 1].exerciseId)?.ar ?: ""
    } else {
        session.exercise.ar
    }
    AppCard {
        Text("وقت الراحة", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
            formatClock(session.restLeft),
            fontSize = 56.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text("التالي: $nextName", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
            OutlinedButton(onClick = { session.addRest(15) }, modifier = Modifier.weight(1f)) { Text("+15 ثانية") }
            Button(onClick = { session.skipRest() }, modifier = Modifier.weight(1f)) { Text("تخطي الراحة") }
        }
    }
}

@Composable
private fun FinishedView(vm: AppViewModel, session: WorkoutSession, painPreset: Boolean, onExit: () -> Unit) {
    var feel by remember { mutableIntStateOf(1) }
    var pain by remember { mutableStateOf(painPreset) }
    var fatigue by remember { mutableStateOf(false) }
    val done = session.anythingDone()
    val setsDone = session.setsDone.sum()

    ScreenScaffold(if (done) "انتهى التمرين 🎉" else "تم إنهاء الجلسة") {
        if (!done) {
            AppCard {
                Text("لم تكمل أي مجموعة في هذه الجلسة، لذلك لن تُحفظ.")
                Spacer8()
                Button(onClick = {
                    vm.discardSession()
                    onExit()
                }, modifier = Modifier.fillMaxWidth()) { Text("العودة للرئيسية") }
            }
            return@ScreenScaffold
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatBox(formatClock(session.elapsed), "زمن الجلسة", Modifier.weight(1f))
            StatBox("$setsDone", "مجموعات مكتملة", Modifier.weight(1f))
            StatBox("${session.skipped.count { it }}", "تمارين تم تخطيها", Modifier.weight(1f))
        }
        AppCard {
            Text("كيف كان التمرين؟", fontWeight = FontWeight.Bold)
            Spacer8()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("سهل", feel == 0, { feel = 0 })
                Chip("مناسب", feel == 1, { feel = 1 })
                Chip("صعب", feel == 2, { feel = 2 })
            }
            Spacer8()
            Row(Modifier.fillMaxWidth().clickable { pain = !pain }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = pain, onCheckedChange = { pain = it })
                Text("شعرت بألم", modifier = Modifier.weight(1f))
            }
            Row(Modifier.fillMaxWidth().clickable { fatigue = !fatigue }, verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = fatigue, onCheckedChange = { fatigue = it })
                Text("إرهاق غير معتاد أو تعافٍ سيئ", modifier = Modifier.weight(1f))
            }
            Spacer8()
            Text(
                "إذا سجلت ألمًا أو إرهاقًا فلن تزيد صعوبة التمارين المقبلة.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        if (pain) {
            WarningBox("الألم ليس جزءًا طبيعيًا من التمرين. ارتح، وأخبر أحد والديك، واستشر طبيبًا إن استمر.")
        }
        Button(
            onClick = {
                vm.saveSession(feel, pain, fatigue, null)
                onExit()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) { Text("حفظ التمرين", fontSize = 16.sp) }
    }
}
