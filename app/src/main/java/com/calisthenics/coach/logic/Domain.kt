package com.calisthenics.coach.logic

data class PlanItem(
    val exerciseId: String,
    val sets: Int,
    val amount: Int,
    val rest: Int
)

data class ExerciseResult(
    val id: String,
    val setsPlanned: Int,
    val setsDone: Int,
    val best: Int,
    val skipped: Boolean
)

data class WorkoutRecord(
    val id: Long,
    val dateKey: String,
    val stage: Int,
    val durationSec: Int,
    val results: List<ExerciseResult>,
    val feedback: Int,      // 0 = easy, 1 = ok, 2 = hard
    val pain: Boolean,
    val fatigue: Boolean
)

data class PlankEntry(val dateKey: String, val seconds: Int)

data class AppState(
    val onboarded: Boolean = false,
    val stage: Int = 1,
    val theme: Int = 0,                       // 0 system, 1 light, 2 dark
    val sound: Boolean = true,
    val vibration: Boolean = true,
    val sessionMinutes: Int = 45,
    val trainingDays: List<Int> = listOf(0, 2, 4), // Saturday-based: Sat=0, Sun=1, Mon=2, Tue=3, Wed=4, Thu=5, Fri=6
    val workouts: List<WorkoutRecord> = emptyList(),
    val records: Map<String, Int> = emptyMap(),
    val targets: Map<String, Int> = emptyMap(),
    val slotIdx: Map<String, Int> = emptyMap(),
    val plankHistory: List<PlankEntry> = emptyList(),
    val skillProgress: Map<String, Int> = emptyMap(),
    val skillTests: Set<String> = emptySet(),
    val stageChecks: Set<String> = emptySet(),
    val painDates: List<String> = emptyList(),
    val plan: List<PlanItem> = emptyList(),
    val planDate: String = "",
    val planReadiness: Int = 0                 // 0 good, 1 tired, 2 pain
)

val DAY_NAMES_SAT0 = listOf("السبت", "الأحد", "الاثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة")
