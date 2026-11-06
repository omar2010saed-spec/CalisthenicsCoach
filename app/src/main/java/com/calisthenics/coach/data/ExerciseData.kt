package com.calisthenics.coach.data

object ExerciseData {
    val all: List<Exercise> = upperExercises + lowerExercises + coreExercises

    private val map: Map<String, Exercise> = all.associateBy { it.id }

    fun get(id: String): Exercise? = map[id]

    fun require(id: String): Exercise = map[id] ?: error("Unknown exercise id: $id")

    val warmups: List<Exercise> = all.filter { it.kind == Kind.WARMUP }
    val cooldowns: List<Exercise> = all.filter { it.kind == Kind.COOLDOWN }
    val strength: List<Exercise> = all.filter { it.kind == Kind.STRENGTH }

    /** Progression chains, ordered from easiest to hardest. */
    val chains: Map<Slot, List<String>> = mapOf(
        Slot.PUSH to listOf("wall_pushup", "knee_pushup", "pushup", "band_pushup", "archer_pushup", "pseudo_planche_lean"),
        Slot.SHOULDER to listOf("band_ext_rot", "band_overhead_pullapart", "pike_hold", "pike_pushup", "wall_handstand_hold"),
        Slot.TRICEPS to listOf("band_kickback", "close_knee_pushup", "diamond_pushup"),
        Slot.BACK to listOf("reverse_snow_angel", "band_pullapart", "prone_y_raise"),
        Slot.BICEPS to listOf("self_resist_curl", "band_curl"),
        Slot.FOREARM to listOf("wrist_rocks", "band_finger_ext"),
        Slot.QUAD to listOf("squat", "reverse_lunge", "split_squat", "jump_squat", "assisted_pistol", "pistol_squat"),
        Slot.HAM to listOf("good_morning", "slider_curl", "sl_rdl"),
        Slot.GLUTE to listOf("glute_bridge", "band_glute_bridge", "single_leg_bridge"),
        Slot.CALF to listOf("calf_raise", "single_calf_raise"),
        Slot.HIP to listOf("clamshell", "fire_hydrant", "band_lateral_walk"),
        Slot.ABS to listOf("crunch", "dead_bug", "reverse_crunch", "bicycle_crunch", "leg_raise", "hollow_hold", "lsit_tuck"),
        Slot.CORE to listOf("plank", "bird_dog", "side_plank", "shoulder_tap_plank", "lsit"),
        Slot.LOWBACK to listOf("superman")
    )

    /** Equivalent-level swaps used on alternate 4-week blocks so the plan does not stay identical forever. */
    val variants: Map<String, String> = mapOf(
        "reverse_snow_angel" to "superman",
        "band_pullapart" to "prone_y_raise",
        "prone_y_raise" to "band_pullapart",
        "glute_bridge" to "donkey_kick",
        "band_glute_bridge" to "donkey_kick",
        "single_leg_bridge" to "band_glute_bridge",
        "squat" to "wall_sit",
        "reverse_lunge" to "split_squat",
        "crunch" to "dead_bug",
        "dead_bug" to "reverse_crunch",
        "reverse_crunch" to "bicycle_crunch",
        "bicycle_crunch" to "leg_raise",
        "plank" to "bird_dog",
        "bird_dog" to "plank",
        "side_plank" to "shoulder_tap_plank",
        "band_ext_rot" to "band_overhead_pullapart",
        "pike_hold" to "band_overhead_pullapart",
        "band_kickback" to "close_knee_pushup",
        "diamond_pushup" to "close_knee_pushup",
        "clamshell" to "fire_hydrant",
        "fire_hydrant" to "band_lateral_walk",
        "wrist_rocks" to "band_finger_ext",
        "self_resist_curl" to "band_curl"
    )

    /** Which slot an exercise belongs to (null for warm-up / cool-down / library-only items). */
    fun slotOf(id: String): Slot? = chains.entries.firstOrNull { id in it.value }?.key

    /** Highest chain index allowed for a slot at a given stage. */
    fun maxIndex(slot: Slot, stage: Int): Int {
        val ids = chains.getValue(slot)
        var best = 0
        ids.forEachIndexed { i, id -> if (require(id).minStage <= stage) best = i }
        return best
    }
}
