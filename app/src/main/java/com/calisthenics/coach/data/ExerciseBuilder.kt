package com.calisthenics.coach.data

internal const val BR_DEFAULT =
    "ازفر أثناء مرحلة المجهود وخذ شهيقًا أثناء الرجوع للوضع الأول. لا تحبس نفسك أبدًا."
internal const val BR_HOLD =
    "تنفّس بهدوء وانتظام طوال التثبيت. لا تحبس نفسك، وإذا فقدت الإيقاع فانهِ المحاولة."
internal const val BR_STRETCH =
    "تنفّس ببطء وعمق، وازفر أثناء الاسترخاء أكثر داخل الإطالة."

@Suppress("LongParameterList")
internal fun ex(
    id: String,
    en: String,
    ar: String,
    muscle: Muscle,
    also: String,
    stage: Int,
    diff: Int,
    sets: Int,
    amount: Int,
    rest: Int,
    anim: Anim,
    start: String,
    steps: List<String>,
    mistakes: List<String>,
    safety: String,
    easier: String?,
    harder: String?,
    kind: Kind = Kind.STRENGTH,
    equip: Equip = Equip.BODY,
    timed: Boolean = false,
    perSide: Boolean = false,
    breathing: String = BR_DEFAULT
) = Exercise(
    id = id, en = en, ar = ar, muscle = muscle, also = also, kind = kind,
    minStage = stage, equip = equip, timed = timed, perSide = perSide,
    sets = sets, amount = amount, rest = rest, difficulty = diff, anim = anim,
    start = start, steps = steps, breathing = breathing, mistakes = mistakes,
    safety = safety, easier = easier, harder = harder
)
