package com.calisthenics.coach.data

enum class Muscle(val ar: String) {
    CHEST("الصدر"),
    BACK("الظهر"),
    SHOULDERS("الأكتاف"),
    BICEPS("البايسبس"),
    TRICEPS("الترايسبس"),
    FOREARMS("الساعد"),
    ABS("عضلات البطن"),
    CORE("ثبات الجذع"),
    LOWER_BACK("أسفل الظهر"),
    QUADS("الفخذ الأمامي"),
    HAMSTRINGS("الفخذ الخلفي"),
    GLUTES("الأرداف"),
    CALVES("السمانة"),
    HIPS("عضلات الورك"),
    FULL("الجسم كامل")
}

enum class Kind { WARMUP, STRENGTH, COOLDOWN }

enum class Equip(val ar: String) {
    BODY("وزن الجسم"),
    BAND("حلقة مقاومة قصيرة")
}

/** Stick-figure pose: 7 joints (x,y pairs, 0..1): head, shoulder, elbow, hand, pelvis, knee, foot. */
class Pose(val p: FloatArray) {
    companion object {
        const val HEAD = 0
        const val SHOULDER = 1
        const val ELBOW = 2
        const val HAND = 3
        const val PELVIS = 4
        const val KNEE = 5
        const val FOOT = 6

        fun of(
            hx: Float, hy: Float, sx: Float, sy: Float, ex: Float, ey: Float, wx: Float, wy: Float,
            px: Float, py: Float, kx: Float, ky: Float, fx: Float, fy: Float
        ) = Pose(floatArrayOf(hx, hy, sx, sy, ex, ey, wx, wy, px, py, kx, ky, fx, fy))
    }
}

/** Two keyframes that the illustration animates between. bandAt = joint index that gets a band ring. */
class Anim(val a: Pose, val b: Pose, val bandAt: Int? = null)

data class Exercise(
    val id: String,
    val en: String,
    val ar: String,
    val muscle: Muscle,
    val also: String,
    val kind: Kind,
    val minStage: Int,
    val equip: Equip,
    val timed: Boolean,
    val perSide: Boolean,
    val sets: Int,
    val amount: Int,
    val rest: Int,
    val difficulty: Int,
    val anim: Anim,
    val start: String,
    val steps: List<String>,
    val breathing: String,
    val mistakes: List<String>,
    val safety: String,
    val easier: String?,
    val harder: String?
) {
    val amountLabel: String
        get() = if (timed) "$amount ثانية" else "$amount تكرار"
}

enum class Slot(val ar: String) {
    PUSH("دفع (صدر)"),
    SHOULDER("أكتاف"),
    TRICEPS("ترايسبس"),
    BACK("ظهر"),
    BICEPS("بايسبس"),
    FOREARM("ساعد"),
    QUAD("فخذ أمامي"),
    HAM("فخذ خلفي"),
    GLUTE("أرداف"),
    CALF("سمانة"),
    HIP("ورك"),
    ABS("بطن"),
    CORE("ثبات الجذع"),
    LOWBACK("أسفل الظهر")
}

data class Stage(
    val number: Int,
    val name: String,
    val summary: String,
    val schedule: List<String>,       // 7 entries, Saturday .. Friday
    val trainingDays: Int,
    val setsRange: String,
    val rest: String,
    val progression: List<String>,    // criteria to move on
    val safety: List<String>,
    val minWorkoutsLast28: Int,
    val pushTarget: Int,              // reps of the stage's main push exercise to show readiness
    val plankTarget: Int              // seconds
)

data class SkillStep(
    val title: String,
    val detail: String,
    val exerciseId: String? = null
)

data class Skill(
    val id: String,
    val en: String,
    val ar: String,
    val homeSafe: Boolean,
    val equipmentNote: String,
    val prerequisites: List<String>,
    val beginner: List<SkillStep>,
    val intermediate: List<SkillStep>,
    val advanced: List<SkillStep>,
    val readinessTests: List<String>,
    val warnings: List<String>
)

data class InfoSection(
    val title: String,
    val points: List<String>
)
