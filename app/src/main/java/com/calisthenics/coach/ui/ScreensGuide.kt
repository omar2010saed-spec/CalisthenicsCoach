package com.calisthenics.coach.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.calisthenics.coach.data.Stages
import com.calisthenics.coach.logic.AppViewModel
import com.calisthenics.coach.logic.DAY_NAMES_SAT0
import com.calisthenics.coach.logic.Progress

@Composable
fun PlanGuideScreen(vm: AppViewModel, onBack: () -> Unit) {
    val s = vm.state
    val stage = Stages.get(s.stage)
    val cycle = Progress.cycleInfo(s)
    val days = s.trainingDays.sorted()

    ScreenScaffold("كيف تعمل خطتك؟", onBack) {
        AppCard {
            Text("أنت الآن", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            Text("المرحلة ${stage.number}: ${stage.name}", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer8()
            Text("الأسبوع ${cycle.weekInBlock} من 4 في الدورة ${cycle.block + 1}", fontWeight = FontWeight.SemiBold)
            Text(
                if (cycle.deload) "هذا أسبوع تخفيف: حجم أقل وبدون زيادة في الصعوبة، ليتعافى جسمك."
                else if (cycle.weekInBlock == 3) "الأسبوع القادم أسبوع تخفيف مقصود، وليس تراجعًا."
                else "أسابيع التقدم: حاول إضافة تكرار بسيط عندما يكون التمرين سهلًا.",
                fontSize = 13.sp
            )
        }

        GuideCard(
            "الفكرة الأساسية",
            listOf(
                "بدون أوزان، نزيد الصعوبة بثلاث طرق: تكرارات أكثر، ثم نسخة أصعب من نفس التمرين، ثم وقت تثبيت أطول.",
                "التقدم الحقيقي يأتي من الانتظام والنوم الجيد والأكل المتوازن أكثر من أي تمرين بعينه.",
                "التمرين هنا بوزن الجسم وحلقات المقاومة القصيرة فقط، ونفضّل تمارين فعّالة وآمنة على تمارين مبهرة."
            )
        )

        AppCard {
            Text("أسبوعك", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text("أيام التدريب من اختيارك في الإعدادات، ونوع الجلسة يتبع ترتيب مرحلتك:", fontSize = 13.sp)
            Spacer8()
            Text(
                stage.rotation.joinToString(" ← ") { it.ar },
                fontWeight = FontWeight.SemiBold
            )
            Spacer8()
            stage.rotation.distinct().forEach { t ->
                Text("• ${t.ar}: ${t.desc}", fontSize = 13.sp)
            }
            Spacer8()
            Text(
                "أيامك الحالية: " + days.joinToString("، ") { DAY_NAMES_SAT0[it] } +
                    ". الموصى به لمرحلتك ${stage.trainingDays} أيام، مع راحة بين كل تمرينين.",
                fontSize = 13.sp
            )
        }

        GuideCard(
            "كيف تكون الجلسة",
            listOf(
                "إحماء من 4 إلى 5 دقائق لتجهيز المفاصل.",
                "من 3 إلى 6 تمارين قوة حسب نوع الجلسة، وعدد المجموعات محسوب على مدة جلستك.",
                "تبريد وإطالة وتنفس لمدة 4 دقائق تقريبًا.",
                "لا تزيد المجموعات من عندك. الحجم المخطط يكفي ويحميك من الإرهاق."
            )
        )

        GuideCard(
            "كيف أتقدم من جلسة لأخرى؟",
            listOf(
                "بعد كل جلسة تقيّمها: سهل أو مناسب أو صعب.",
                "سهل وأكملت كل المجموعات: يزيد الهدف قليلًا في المرة القادمة (تكرار واحد أو 5 ثوانٍ).",
                "مناسب: يبقى كما هو حتى تثبته.",
                "صعب أو لم تكمل المجموعات: يقل الهدف قليلًا، وإن استمر ذلك ينزل بك التطبيق لنسخة أسهل.",
                "وصلت لأعلى هدف بسهولة: ينقلك لنسخة أصعب من التمرين إن كانت مرحلتك تسمح.",
                "إذا سجلت ألمًا أو إرهاقًا غير معتاد: لا تُرفع الصعوبة أبدًا."
            )
        )

        GuideCard(
            "قاعدة الجهد",
            listOf(
                "اترك دائمًا تكرارًا أو اثنين في الخزان؛ لا تصل لأقصى جهدك.",
                "انزل ببطء من 2 إلى 3 ثوانٍ وارتفع بقوة وتحكم.",
                "الشكل الصحيح أهم من عدد التكرارات. إن اهتز الشكل فتوقف أو استبدل التمرين بنسخة أسهل."
            )
        )

        GuideCard(
            "دورة الأربعة أسابيع",
            listOf(
                "الأسابيع 1 إلى 3: تقدم عادي.",
                "الأسبوع 4: تخفيف مقصود (مجموعات وتكرارات أقل وبدون زيادة) ليتعافى الجسم ويعود أقوى.",
                "في الدورة التالية تتبدل بعض التمارين بتمارين مكافئة في المستوى، فلا تمل ولا يعتاد جسمك على نفس الشيء."
            )
        )

        GuideCard(
            "متى أنتقل للمرحلة التالية؟",
            listOf(
                "لا يوجد موعد ثابت. يعتمد الانتقال على الالتزام والتقنية والتعافي وقدرتك الفعلية.",
                "الشروط الكاملة وزر الانتقال في صفحة «المراحل والتقدم».",
                "المرحلة 6 مفتوحة: الهدف إتقان المهارات بأمان وليس الإسراع."
            )
        )

        GuideCard(
            "هل سأبقى على نفس النظام دائمًا؟",
            listOf(
                "لا. الخطة تتغير عليك تلقائيًا: تقدم داخل المرحلة، وتنويع كل 4 أسابيع، وانتقال بين المراحل.",
                "بعد المرحلة 6 تستمر الدورات بنسخ أصعب وتثبيتات أطول، لكن التقدم الكبير بعدها يحتاج معدات.",
                "كلما تغيرت أهدافك أو مواعيدك، عدّل أيام التدريب ومدة الجلسة من الإعدادات."
            )
        )

        GuideCard(
            "ما الذي يفتح لك تقدمًا أكبر لاحقًا؟ (اختياري)",
            listOf(
                "عقلة تُثبَّت في الحائط بشكل سليم، تُشترى من محل رياضي موثوق وتُركّب بمساعدة شخص بالغ. هذه أهم إضافة للظهر والبايسبس.",
                "متوازيان صغيران (Parallettes) للغطس وجلسة L.",
                "لا تعلّق أي شيء بباب أو أثاث أو شجرة غير مضمونة، ولا تبتكر نقاط تثبيت للحلقات."
            )
        )

        GuideCard(
            "حدود البرنامج بصراحة",
            listOf(
                "تمارين السحب (ظهر وبايسبس) محدودة جدًا بدون عقلة؛ الحلقات القصيرة لا تعوّضها بالكامل.",
                "لذلك نركز على ما يمكن تدريبه بجودة: الدفع والساقان والجذع والأرداف، ونقوّي الظهر بتمارين مساعدة.",
                "لا يعد التطبيق بنتائج محددة في الطول أو العضلات أو شكل الجسم."
            )
        )

        GuideCard(
            "الإنترنت",
            listOf(
                "التطبيق كله يعمل بدون إنترنت. ولا يطلب إذن إنترنت أصلًا.",
                "زر «شاهد فيديو» في صفحة التمرين يفتح بحثًا على يوتيوب في متصفحك فقط عند الضغط عليه، لتشاهد شرحًا حقيقيًا للحركة."
            )
        )
    }
}

@Composable
private fun GuideCard(title: String, points: List<String>) {
    AppCard {
        Text(title, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer8()
        BulletList(points)
    }
}
