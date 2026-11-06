package com.calisthenics.coach.logic

import com.calisthenics.coach.data.ExerciseData
import com.calisthenics.coach.data.Exercise
import com.calisthenics.coach.data.Kind
import com.calisthenics.coach.data.Slot

/**
 * Builds a workout from the user's stage, equipment (only what is in the exercise library), session length
 * and recovery state. It never raises difficulty when pain / fatigue / poor recovery is reported.
 */
object Generator {

    private const val MAX_STRENGTH_SETS = 20

    private val templateA = listOf(Slot.PUSH, Slot.QUAD, Slot.SHOULDER, Slot.ABS, Slot.TRICEPS, Slot.CALF)
    private val templateB = listOf(Slot.GLUTE, Slot.BACK, Slot.CORE, Slot.HAM, Slot.BICEPS, Slot.LOWBACK)
    private val templateC = listOf(Slot.PUSH, Slot.QUAD, Slot.CORE, Slot.BACK, Slot.HIP, Slot.ABS, Slot.FOREARM)

    /** True if recent workouts show pain, unusual fatigue or a pain report in the last 3 days. */
    fun recoveryFlag(s: AppState): Boolean {
        val today = Dates.todayKey()
        if (s.painDates.any { Dates.daysBetween(it, today) in 0..3 }) return true
        val last = s.workouts.maxByOrNull { it.id } ?: return false
        return (last.pain || last.fatigue) && Dates.daysBetween(last.dateKey, today) <= 3
    }

    fun slotIndex(s: AppState, slot: Slot): Int {
        val max = ExerciseData.maxIndex(slot, s.stage)
        val stored = s.slotIdx[slot.name]
        return (stored ?: ExerciseData.maxIndex(slot, 1)).coerceIn(0, max)
    }

    fun targetFor(s: AppState, e: Exercise): Int = s.targets[e.id] ?: e.amount

    private fun workSeconds(e: Exercise, amount: Int): Int = if (e.timed) amount else amount * 4

    /** Estimated duration in seconds for a plan item. */
    fun estimateSeconds(item: PlanItem): Int {
        val e = ExerciseData.get(item.exerciseId) ?: return 0
        val sideFactor = if (e.perSide) 2 else 1
        val work = workSeconds(e, item.amount) * sideFactor
        return item.sets * (work + item.rest) + 15
    }

    fun estimateMinutes(plan: List<PlanItem>): Int =
        ((plan.sumOf { estimateSeconds(it) }) / 60.0).let { Math.ceil(it).toInt() }

    private fun item(e: Exercise, sets: Int, amount: Int) =
        PlanItem(e.id, sets.coerceAtLeast(1), amount.coerceAtLeast(1), e.rest)

    private fun recoverySession(): List<PlanItem> {
        val ids = listOf(
            "march_in_place", "cat_cow", "hip_circles", "arm_circles", "bird_dog", "glute_bridge",
            "childs_pose", "hamstring_stretch", "hip_flexor_stretch", "deep_breathing"
        )
        return ids.mapNotNull { ExerciseData.get(it) }.map { e ->
            val sets = if (e.id == "bird_dog" || e.id == "glute_bridge") 2 else 1
            val amount = if (e.id == "bird_dog" || e.id == "glute_bridge") 6 else e.amount
            PlanItem(e.id, sets, amount, 30)
        }
    }

    fun generate(s: AppState, readiness: Int): List<PlanItem> {
        // Pain: no strength work, light mobility only.
        if (readiness >= 2) return recoverySession()

        val tired = readiness == 1 || recoveryFlag(s)
        val baseSets = when {
            s.stage <= 1 -> 2
            else -> 3
        }
        val sets = if (tired) (baseSets - 1).coerceAtLeast(1) else baseSets

        val warmups = listOf("march_in_place", "arm_circles", "hip_circles", "cat_cow", "wrist_rotations")
            .mapNotNull { ExerciseData.get(it) }
            .let { all ->
                // wrist work is only needed for upper-body days; 4 warm-ups keep it around 4-5 minutes.
                all.take(4)
            }
            .map { PlanItem(it.id, 1, it.amount, 10) }

        val coolIds = listOf("childs_pose", "chest_stretch", "hamstring_stretch", "deep_breathing")
        val cooldowns = coolIds.mapNotNull { ExerciseData.get(it) }.map { PlanItem(it.id, 1, it.amount, 5) }

        val warmMinutes = estimateMinutes(warmups)
        val coolMinutes = estimateMinutes(cooldowns)
        val budgetSeconds = ((s.sessionMinutes - warmMinutes - coolMinutes).coerceAtLeast(10)) * 60

        val sessionNumber = s.workouts.size
        val template = when (sessionNumber % 3) {
            0 -> templateA
            1 -> templateB
            else -> templateC
        }

        val chosen = ArrayList<PlanItem>()
        val usedIds = HashSet<String>()
        var spent = 0
        var totalSets = 0
        for (slot in template) {
            val idx = slotIndex(s, slot)
            val id = ExerciseData.chains.getValue(slot)[idx]
            if (id in usedIds) continue
            val e = ExerciseData.require(id)
            val amount = targetFor(s, e)
            val planItem = item(e, sets, amount)
            val cost = estimateSeconds(planItem)
            if (spent + cost > budgetSeconds && chosen.isNotEmpty()) continue
            if (totalSets + planItem.sets > MAX_STRENGTH_SETS) break
            chosen.add(planItem)
            usedIds.add(id)
            spent += cost
            totalSets += planItem.sets
        }

        // The stage's plank target is trained once per session as a core item if missing.
        if (chosen.none { ExerciseData.get(it.exerciseId)?.muscle?.name == "CORE" } && spent < budgetSeconds) {
            val plank = ExerciseData.require("plank")
            chosen.add(item(plank, 2, targetFor(s, plank)))
        }

        return warmups + chosen + cooldowns
    }

    /** Alternative exercises that can replace [current] in a plan (same muscle group, allowed for the stage). */
    fun alternatives(s: AppState, current: Exercise, plan: List<PlanItem>): List<Exercise> {
        val inPlan = plan.map { it.exerciseId }.toSet()
        return ExerciseData.all
            .filter { it.kind == current.kind && it.muscle == current.muscle }
            .filter { it.id != current.id && it.id !in inPlan }
            .filter { it.minStage <= s.stage }
            .sortedBy { it.difficulty }
    }

    fun replace(s: AppState, plan: List<PlanItem>, index: Int, newId: String): List<PlanItem> {
        val e = ExerciseData.get(newId) ?: return plan
        val old = plan.getOrNull(index) ?: return plan
        val amount = targetFor(s, e)
        val updated = PlanItem(e.id, old.sets, amount, e.rest)
        return plan.toMutableList().also { it[index] = updated }
    }

    fun isStrength(id: String): Boolean = ExerciseData.get(id)?.kind == Kind.STRENGTH
}
