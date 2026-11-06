package com.calisthenics.coach.ui

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.Info
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.logic.DAY_NAMES_SAT0
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

// ============================================================ nutrition

@Composable
fun NutritionScreen(onBack: () -> Unit) {
    ScreenScaffold("التغذية والتعافي", onBack) {
        Text(
            "معلومات تعليمية عامة وليست نصيحة طبية أو غذائية شخصية.",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.outline
        )
        Info.nutrition.forEach { sec ->
            AppCard {
                Text(sec.title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
                Spacer8()
                BulletList(sec.points)
            }
        }
    }
}

// ============================================================ settings

private fun readText(context: Context, uri: Uri): String? = try {
    context.contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
} catch (e: Exception) {
    null
}

private fun writeText(context: Context, uri: Uri, text: String): Boolean = try {
    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) } != null
} catch (e: Exception) {
    false
}

@Composable
fun SettingsScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.state
    val context = LocalContext.current
    var message by remember { mutableStateOf<String?>(null) }
    var confirmReset by remember { mutableStateOf(false) }
    var minutes by remember(s.sessionMinutes) { mutableStateOf(s.sessionMinutes.toFloat()) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            message = if (writeText(context, uri, vm.exportText())) "تم حفظ النسخة الاحتياطية بنجاح." else "تعذر حفظ الملف."
        }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val text = readText(context, uri)
            message = when {
                text == null -> "تعذر قراءة الملف."
                vm.importText(text) -> "تمت استعادة بياناتك بنجاح."
                else -> "هذا الملف ليس نسخة احتياطية صالحة لهذا التطبيق."
            }
        }
    }

    ScreenScaffold("الإعدادات", onBack) {
        AppCard {
            Text("المظهر", fontWeight = FontWeight.Bold)
            Spacer8()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip("حسب النظام", s.theme == 0, { vm.setTheme(0) })
                Chip("فاتح", s.theme == 1, { vm.setTheme(1) })
                Chip("داكن", s.theme == 2, { vm.setTheme(2) })
            }
        }

        AppCard {
            SwitchRow("المؤثرات الصوتية", s.sound) { vm.setSound(it) }
            SwitchRow("الاهتزاز", s.vibration) { vm.setVibration(it) }
        }

        AppCard {
            Text("مدة الجلسة: ${minutes.roundToInt()} دقيقة", fontWeight = FontWeight.Bold)
            Slider(
                value = minutes,
                onValueChange = { minutes = it },
                onValueChangeFinished = { vm.setSessionMinutes((minutes / 5f).roundToInt() * 5) },
                valueRange = 20f..60f,
                steps = 7
            )
            Text("الحد الأقصى 60 دقيقة. الجلسات الأقصر ممتازة للبداية.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
        }

        AppCard {
            Text("أيام التدريب", fontWeight = FontWeight.Bold)
            Text("اختر من يومين إلى 5 أيام. يُنصح بـ 3 أيام مع يوم راحة بين كل تمرينين.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Spacer8()
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DAY_NAMES_SAT0.forEachIndexed { i, n ->
                    Chip(n, i in s.trainingDays, { vm.toggleTrainingDay(i) })
                }
            }
        }

        AppCard {
            Text("النسخ الاحتياطي والاستعادة", fontWeight = FontWeight.Bold)
            Text("بياناتك محفوظة على هاتفك فقط. يمكنك حفظ نسخة في ملف محلي واستعادتها لاحقًا.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Spacer8()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val stamp = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
                        exportLauncher.launch("calisthenics-backup-$stamp.json")
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("حفظ نسخة") }
                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream")) },
                    modifier = Modifier.weight(1f)
                ) { Text("استعادة نسخة") }
            }
        }

        AppCard {
            Text("حذف كل البيانات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Text("يحذف التقدم والسجلات من هذا الهاتف نهائيًا.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Spacer8()
            OutlinedButton(onClick = { confirmReset = true }) { Text("حذف البيانات") }
        }

        AppCard {
            Text("عن التطبيق", fontWeight = FontWeight.Bold)
            Spacer8()
            Text(
                "يعمل بدون إنترنت ولا يجمع أي بيانات. هذا التطبيق للتثقيف العام وليس بديلًا عن الطبيب أو المدرب. " +
                    "لا يعد بنتائج محددة في الطول أو العضلات أو شكل الجسم.",
                fontSize = 13.sp
            )
        }
    }

    message?.let { m ->
        AlertDialog(
            onDismissRequest = { message = null },
            confirmButton = { TextButton(onClick = { message = null }) { Text("حسنًا") } },
            text = { Text(m) }
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("حذف كل البيانات؟") },
            text = { Text("لا يمكن التراجع عن هذا الإجراء. يُفضّل حفظ نسخة احتياطية أولًا.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetAll()
                    confirmReset = false
                }) { Text("حذف") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("إلغاء") } }
        )
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
