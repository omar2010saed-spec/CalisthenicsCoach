package com.calisthenics.coach.logic

import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.data.Kind
import com.calisthenics.coach.data.Stages
import java.util.Calendar
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

data class ReadinessItem(
    val key: String,
    val label: String,
    val done: Boolean,
    val manual: Boolean
)

data class Milestone(val title: String, val achieved: Boolean)

data class CycleInfo(val weekNumber: Int, val block: Int, val weekInBlock: Int, val deload: Boolean)

object Progress {

    /** Training is organised in 4-week blocks counted from the first completed workout; week 4 is a lighter "deload" week. */
    fun cycleInfo(s: AppState): CycleInfo {
        val first = s.workouts.minOfOrNull { it.dateKey }
        val weeks = if (first == null) 0 else (Dates.daysBetween(first, Dates.todayKey()) / 7).coerceAtLeast(0)
        val inBlock = weeks % 4 + 1
        return CycleInfo(weeks + 1, weeks / 4, inBlock, inBlock == 4)
    }

    // ---------------------------------------------------------------- applying a finished workout

    fun applyWorkout(s: AppState, record: WorkoutRecord, extraPlankSeconds: Int?): AppState {
        val records = s.records.toMutableMap()
        val targets = s.targets.toMutableMap()
        val slotIdx = s.slotIdx.toMutableMap()
        val plank = s.plankHistory.toMutableList()
        val pain = s.painDates.toMutableList()

        if (record.pain && record.dateKey !in pain) pain.add(record.dateKey)

        val frozen = record.pain || record.fatigue || cycleInfo(s).deload

        for (r in record.results) {
            val e = ExerciseData.get(r.id) ?: continue
            if (e.kind != Kind.STRENGTH) continue
            if (r.skipped || r.setsDone <= 0) continue

            records[r.id] = max(records[r.id] ?: 0, r.best)
            if (r.id == "plank" && r.best > 0) plank.add(PlankEntry(record.dateKey, r.best))

            if (frozen) continue

            val base = e.amount
            val step = if (e.timed) 5 else 1
            val cap = if (e.timed) base + 25 else base + 6
            val floor = ceil(base * 0.6).toInt().coerceAtLeast(1)
            val current = targets[r.id] ?: base
            val complete = r.setsDone >= r.setsPlanned

            when {
                complete && record.feedback == 0 -> {
                    var promoted = false
                    if (current >= cap) {
                        // Mastered this version: move to the next harder one if the current stage allows it.
                        val slot = ExerciseData.slotOf(r.id)
                        if (slot != null) {
                            val idx = Generator.slotIndex(s, slot)
                            if (ExerciseData.chains.getValue(slot)[idx] == r.id && idx < ExerciseData.maxIndex(slot, s.stage)) {
                                slotIdx[slot.name] = idx + 1
                                targets.remove(r.id)
                                promoted = true
                            }
                        }
                    }
                    if (!promoted) targets[r.id] = min(cap, current + step)
                }
                !complete || record.feedback == 2 -> {
                    if (current > floor) {
                        targets[r.id] = max(floor, current - step)
                    } else {
                        // Already at the lowest target: step back to an easier exercise in the chain.
                        val slot = ExerciseData.slotOf(r.id)
                        if (slot != null) {
                            val idx = Generator.slotIndex(s, slot)
                            if (ExerciseData.chains.getValue(slot)[idx] == r.id && idx > 0) {
                                slotIdx[slot.name] = idx - 1
                            }
                        }
                    }
                }
            }
        }

        if (extraPlankSeconds != null && extraPlankSeconds > 0) {
            plank.add(PlankEntry(record.dateKey, extraPlankSeconds))
            records["plank"] = max(records["plank"] ?: 0, extraPlankSeconds)
        }

        return s.copy(
            workouts = s.workouts + record,
            records = records,
            targets = targets,
            slotIdx = slotIdx,
            plankHistory = plank,
            painDates = pain,
            plan = emptyList(),
            planDate = ""
        )
    }

    // ---------------------------------------------------------------- statistics

    fun workoutsLastDays(s: AppState, days: Int): Int {
        val today = Dates.todayKey()
        return s.workouts.count { Dates.daysBetween(it.dateKey, today) in 0 until days }
    }

    fun workoutsInWeek(s: AppState, weekStart: String): Int {
        val end = Dates.addDays(weekStart, 6)
        return s.workouts.count { it.dateKey >= weekStart && it.dateKey <= end }
    }

    /** Workout counts for the last [weeks] weeks, oldest first. Each entry is (week start key, count). */
    fun weeklyCounts(s: AppState, weeks: Int): List<Pair<String, Int>> {
        val current = Dates.weekStartKey(Calendar.getInstance())
        return (weeks - 1 downTo 0).map { back ->
            val start = Dates.addDays(current, -7 * back)
            start to workoutsInWeek(s, start)
        }
    }

    fun streakWeeks(s: AppState): Int {
        val need = max(1, s.trainingDays.size - 1)
        val current = Dates.weekStartKey(Calendar.getInstance())
        var streak = 0
        var week = current
        // The current week may still be in progress; do not break the streak because of it.
        if (workoutsInWeek(s, week) >= need) streak++
        week = Dates.addDays(week, -7)
        while (workoutsInWeek(s, week) >= need) {
            streak++
            week = Dates.addDays(week, -7)
            if (streak > 520) break
        }
        return streak
    }

    fun bestPlank(s: AppState): Int = max(s.records["plank"] ?: 0, s.plankHistory.maxOfOrNull { it.seconds } ?: 0)

    fun totalMinutes(s: AppState): Int = s.workouts.sumOf { it.durationSec } / 60

    fun workoutDates(s: AppState): Set<String> = s.workouts.map { it.dateKey }.toSet()

    // ---------------------------------------------------------------- milestones

    fun milestones(s: AppState): List<Milestone> {
        val n = s.workouts.size
        val push = max(s.records["pushup"] ?: 0, 0)
        val plank = bestPlank(s)
        return listOf(
            Milestone("أول تمرين مكتمل", n >= 1),
            Milestone("5 تمرينات مكتملة", n >= 5),
            Milestone("10 تمرينات مكتملة", n >= 10),
            Milestone("25 تمرينًا مكتملًا", n >= 25),
            Milestone("50 تمرينًا مكتملًا", n >= 50),
            Milestone("3 أسابيع متتالية من الالتزام", streakWeeks(s) >= 3),
            Milestone("8 ضغطات ركبتين في مجموعة", (s.records["knee_pushup"] ?: 0) >= 8),
            Milestone("5 ضغطات عادية في مجموعة", push >= 5),
            Milestone("10 ضغطات عادية في مجموعة", push >= 10),
            Milestone("20 ضغطة عادية في مجموعة", push >= 20),
            Milestone("بلانك 30 ثانية", plank >= 30),
            Milestone("بلانك 60 ثانية", plank >= 60),
            Milestone("بلانك 90 ثانية", plank >= 90),
            Milestone("20 سكوات في مجموعة", (s.records["squat"] ?: 0) >= 20),
            Milestone("الانتقال إلى مرحلة أعلى", s.stage > 1)
        )
    }

    // ---------------------------------------------------------------- stage advancement

    fun readiness(s: AppState): List<ReadinessItem> {
        val st = Stages.get(s.stage)
        val today = Dates.todayKey()
        val pushId = Stages.pushTestExercise(s.stage)
        val pushName = ExerciseData.get(pushId)?.ar ?: pushId
        val recent = workoutsLastDays(s, 28)
        val painFree = s.workouts.none {
            (it.pain || it.fatigue) && Dates.daysBetween(it.dateKey, today) in 0..13
        } && s.painDates.none { Dates.daysBetween(it, today) in 0..13 }
        val pushBest = s.records[pushId] ?: 0
        val plank = bestPlank(s)

        return listOf(
            ReadinessItem(
                "consistency", "الالتزام: ${st.minWorkoutsLast28} تمرينات على الأقل خلال آخر 28 يومًا (لديك $recent)",
                recent >= st.minWorkoutsLast28, false
            ),
            ReadinessItem(
                "pain", "لا ألم ولا إرهاق غير معتاد مسجل خلال آخر 14 يومًا",
                painFree, false
            ),
            ReadinessItem(
                "push", "القوة: $pushName ${st.pushTarget} تكرارات في مجموعة (أفضل رقم لديك $pushBest)",
                pushBest >= st.pushTarget, false
            ),
            ReadinessItem(
                "plank", "الثبات: بلانك ${st.plankTarget} ثانية (أفضل رقم لديك $plank)",
                plank >= st.plankTarget, false
            ),
            ReadinessItem(
                "${s.stage}|tech", "التقنية: أؤدي حركات هذه المرحلة بشكل نظيف وبطيء ومتحكم به",
                "${s.stage}|tech" in s.stageChecks, true
            ),
            ReadinessItem(
                "${s.stage}|recovery", "التعافي: أنام جيدًا وأتعافى بين التمارين",
                "${s.stage}|recovery" in s.stageChecks, true
            )
        )
    }

    fun canAdvance(s: AppState): Boolean = s.stage < Stages.all.size && readiness(s).all { it.done }

    fun advance(s: AppState): AppState {
        if (!canAdvance(s)) return s
        val next = s.stage + 1
        val idx = s.slotIdx.toMutableMap()
        for ((slot, ids) in ExerciseData.chains) {
            val maxIdx = ExerciseData.maxIndex(slot, next)
            val cur = Generator.slotIndex(s, slot)
            idx[slot.name] = max(cur, maxIdx).coerceAtMost(ids.size - 1)
        }
        return s.copy(stage = next, slotIdx = idx, plan = emptyList(), planDate = "")
    }

    fun toggleStageCheck(s: AppState, key: String): AppState {
        val set = s.stageChecks.toMutableSet()
        if (!set.add(key)) set.remove(key)
        return s.copy(stageChecks = set)
    }

    // ---------------------------------------------------------------- skills

    fun toggleSkillTest(s: AppState, key: String): AppState {
        val set = s.skillTests.toMutableSet()
        if (!set.add(key)) set.remove(key)
        return s.copy(skillTests = set)
    }

    fun setSkillLevel(s: AppState, skillId: String, level: Int): AppState {
        val m = s.skillProgress.toMutableMap()
        m[skillId] = level.coerceIn(0, 3)
        return s.copy(skillProgress = m)
    }
}
