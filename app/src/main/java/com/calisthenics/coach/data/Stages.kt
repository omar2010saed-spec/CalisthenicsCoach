package com.calisthenics.coach.data

object Stages {

    val all: List<Stage> = listOf(
        Stage(
            number = 1,
            name = "مبتدئ تمامًا",
            summary = "بناء العادة وتعلم الشكل الصحيح للحركات الأساسية (حائط، ركبتان، سكوات، بلانك) بحجم تدريب خفيف.",
            rotation = listOf(SessionType.FULL_A, SessionType.FULL_B, SessionType.FULL_C),
            trainingDays = 3,
            setsRange = "2 إلى 3 مجموعات",
            rest = "60 إلى 90 ثانية بين المجموعات",
            progression = listOf(
                "إتقان ضغط الركبتين 3 مجموعات × 8 تكرارات بشكل نظيف وبطيء",
                "بلانك الساعدين 30 ثانية بشكل مستقيم",
                "سكوات 3 × 12 بدون ألم في الركبتين",
                "الالتزام بعدد التمرينات المطلوبة خلال آخر 28 يومًا",
                "نوم جيد وعدم وجود ألم أو إرهاق غير معتاد في آخر أسبوعين"
            ),
            safety = listOf(
                "تعلّم الحركة ببطء قبل زيادة التكرارات",
                "توقف فورًا عند أي ألم حاد (وليس مجرد حرق عضلي)",
                "لا تتدرب أيام متتالية في هذه المرحلة"
            ),
            minWorkoutsLast28 = 8,
            pushTarget = 8,
            plankTarget = 30
        ),
        Stage(
            number = 2,
            name = "مبتدئ",
            summary = "الانتقال من ضغط الركبتين إلى الضغط العادي، وتقوية الجذع والأرداف والورك بحجم معتدل.",
            rotation = listOf(SessionType.FULL_A, SessionType.FULL_B, SessionType.FULL_C),
            trainingDays = 3,
            setsRange = "3 مجموعات",
            rest = "60 إلى 90 ثانية",
            progression = listOf(
                "ضغط عادي 3 × 6 بشكل نظيف دون هبوط الوركين",
                "بلانك 40 ثانية",
                "اندفاع للخلف 3 × 8 لكل رجل بتحكم",
                "الالتزام بعدد التمرينات المطلوبة خلال آخر 28 يومًا",
                "لا ألم ولا إرهاق غير معتاد خلال آخر أسبوعين"
            ),
            safety = listOf(
                "إذا انهار شكل الحركة فخذ راحة أطول أو انتقل لنسخة أسهل",
                "لا تضف مجموعات إضافية فوق المخطط",
                "حافظ على يوم راحة بين كل تمرينين"
            ),
            minWorkoutsLast28 = 10,
            pushTarget = 8,
            plankTarget = 40
        ),
        Stage(
            number = 3,
            name = "متوسط",
            summary = "إدخال ضغط بمقاومة الحلقة، وضغط الباي للأكتاف، وتمارين الأرداف أحادية الجانب.",
            rotation = listOf(SessionType.FULL_A, SessionType.FULL_B, SessionType.FULL_C),
            trainingDays = 3,
            setsRange = "3 مجموعات",
            rest = "75 إلى 90 ثانية",
            progression = listOf(
                "ضغط عادي 3 × 10 بتحكم كامل",
                "بلانك 60 ثانية",
                "جسر الأرداف برجل واحدة 3 × 6 لكل رجل",
                "الالتزام بعدد التمرينات المطلوبة خلال آخر 28 يومًا",
                "لا ألم ولا إرهاق غير معتاد خلال آخر أسبوعين"
            ),
            safety = listOf(
                "افحص حلقات المقاومة قبل كل جلسة",
                "لا تقفز أو تفرط في السرعة بعد",
                "أبلغ شخصًا بالغًا إن شعرت بألم متكرر"
            ),
            minWorkoutsLast28 = 10,
            pushTarget = 10,
            plankTarget = 60
        ),
        Stage(
            number = 4,
            name = "فوق المتوسط",
            summary = "ضغط الماسة وتمارين القفز الخفيف وتمارين الرجل الواحدة وتثبيتات البطن الأصعب.",
            rotation = listOf(SessionType.PUSH, SessionType.LEGS, SessionType.PULL, SessionType.MIX),
            trainingDays = 4,
            setsRange = "3 مجموعات",
            rest = "75 إلى 120 ثانية",
            progression = listOf(
                "ضغط الماسة 3 × 6 بتحكم",
                "بلانك 75 ثانية",
                "سكوات بالقفز 3 × 8 بهبوط ناعم",
                "تثبيت الجسم المجوف 15 ثانية",
                "الالتزام بعدد التمرينات المطلوبة خلال آخر 28 يومًا وبدون ألم"
            ),
            safety = listOf(
                "تمارين القفز تتطلب هبوطًا ناعمًا وأرضًا غير زلقة",
                "راقب الرسغين والمرفقين وخذ يوم راحة إضافي عند الحاجة",
                "لا تضف حجمًا كبيرًا بسرعة"
            ),
            minWorkoutsLast28 = 12,
            pushTarget = 12,
            plankTarget = 75
        ),
        Stage(
            number = 5,
            name = "متقدم",
            summary = "تمارين الرجل الواحدة المساعدة، ضغط الرامي، وتثبيتات الوقوف على اليدين بمواجهة الحائط.",
            rotation = listOf(SessionType.PUSH, SessionType.LEGS, SessionType.PULL, SessionType.MIX),
            trainingDays = 4,
            setsRange = "3 مجموعات",
            rest = "90 إلى 120 ثانية",
            progression = listOf(
                "ضغط الرامي 3 × 4 لكل جانب",
                "بيستول مساعد 3 × 4 لكل رجل بتحكم",
                "بلانك 90 ثانية",
                "تثبيت الوقوف على اليدين بمواجهة الحائط 15 ثانية بثبات",
                "الالتزام بعدد التمرينات المطلوبة خلال آخر 28 يومًا وبدون ألم"
            ),
            safety = listOf(
                "لا تُجرب مهارات متقدمة بدون وجود شخص بالغ قريب عند اللزوم",
                "تأكد من تسخين الرسغين والكتفين",
                "الراحة والنوم جزء أساسي من التقدم"
            ),
            minWorkoutsLast28 = 12,
            pushTarget = 12,
            plankTarget = 90
        ),
        Stage(
            number = 6,
            name = "مهارات كاليسثينكس متقدمة",
            summary = "تعلّم المهارات المتقدمة التي يمكن تدريبها بأمان على الأرض فقط، مع حدود واضحة للمهارات التي تحتاج معدات.",
            rotation = listOf(SessionType.PUSH, SessionType.LEGS, SessionType.PULL, SessionType.MIX),
            trainingDays = 4,
            setsRange = "3 مجموعات (جودة قبل الكمية)",
            rest = "90 إلى 120 ثانية",
            progression = listOf(
                "هذه المرحلة الأخيرة؛ الهدف هو إتقان المهارات بأمان لا الإسراع",
                "الإمالة في البلانش الكاذب 15 ثانية بشكل نظيف",
                "جلسة L على الأرض 8 ثوانٍ",
                "بيستول بتحكم كامل (ثلاث تكرارات لكل رجل)"
            ),
            safety = listOf(
                "المهارات التي تحتاج عقلة أو متوازيين غير متاحة مع معداتك الحالية",
                "تجنب أي محاولة مرتجلة للتعلق من أبواب أو أثاث",
                "توقف عند ألم الرسغ أو المرفق أو الكتف واستشر مختصًا"
            ),
            minWorkoutsLast28 = 12,
            pushTarget = 12,
            plankTarget = 90
        )
    )

    fun get(n: Int): Stage = all[(n - 1).coerceIn(0, all.size - 1)]

    /** Exercises used as a reference for the "main push" strength test for each stage. */
    fun pushTestExercise(stage: Int): String = when (stage) {
        1 -> "knee_pushup"
        2 -> "pushup"
        3 -> "pushup"
        4 -> "diamond_pushup"
        else -> "archer_pushup"
    }
}
