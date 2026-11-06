package com.calisthenics.coach.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.calisthenics.coach.data.ExerciseData

enum class Phase { READY, WORK, REST, FINISHED }

/**
 * State machine for an interactive workout. All timing is driven by tick(), which the UI calls once per second
 * while the screen is visible - no network or background service is involved.
 */
class WorkoutSession(initial: List<PlanItem>) {

    val items = mutableStateListOf<PlanItem>().also { it.addAll(initial) }

    var index by mutableIntStateOf(0)
        private set
    var currentSet by mutableIntStateOf(1)
        private set
    var phase by mutableStateOf(Phase.READY)
        private set
    var restLeft by mutableIntStateOf(0)
        private set
    var workLeft by mutableIntStateOf(0)
        private set
    var side by mutableIntStateOf(1)
        private set
    var paused by mutableStateOf(false)
        private set
    var elapsed by mutableIntStateOf(0)
        private set
    var reps by mutableIntStateOf(initial.firstOrNull()?.amount ?: 0)

    val setsDone = mutableStateListOf<Int>().also { l -> initial.forEach { _ -> l.add(0) } }
    val best = mutableStateListOf<Int>().also { l -> initial.forEach { _ -> l.add(0) } }
    val skipped = mutableStateListOf<Boolean>().also { l -> initial.forEach { _ -> l.add(false) } }

    private var restThenNext = false

    /** Signals for sound/vibration: 1 = short tick, 2 = completion. Consumed by the UI. */
    var signal by mutableIntStateOf(0)
        private set

    fun consumeSignal() {
        signal = 0
    }

    val item: PlanItem get() = items[index]
    val exercise get() = ExerciseData.require(items[index].exerciseId)
    val isLast: Boolean get() = index == items.lastIndex

    fun tick() {
        if (paused || phase == Phase.FINISHED) return
        elapsed++
        when (phase) {
            Phase.REST -> {
                restLeft--
                if (restLeft in 1..3) signal = 1
                if (restLeft <= 0) endRest()
            }
            Phase.WORK -> {
                workLeft--
                if (workLeft in 1..3) signal = 1
                if (workLeft <= 0) workFinished()
            }
            else -> Unit
        }
    }

    fun togglePause() {
        if (phase == Phase.FINISHED) return
        paused = !paused
    }

    fun startTimed() {
        if (phase != Phase.READY) return
        phase = Phase.WORK
        workLeft = item.amount
    }

    private fun workFinished() {
        val e = exercise
        if (e.perSide && side == 1) {
            side = 2
            workLeft = item.amount
            signal = 2
        } else {
            completeSet(item.amount)
        }
    }

    /** Called when the user finishes a rep-based set (or a timed set is done). [actual] is the number achieved. */
    fun completeSet(actual: Int) {
        if (phase == Phase.FINISHED) return
        setsDone[index] = setsDone[index] + 1
        best[index] = maxOf(best[index], actual)
        signal = 2
        side = 1
        val lastSet = setsDone[index] >= item.sets
        if (lastSet && isLast) {
            phase = Phase.FINISHED
            return
        }
        restThenNext = lastSet
        restLeft = item.rest.coerceAtLeast(5)
        phase = Phase.REST
    }

    fun skipRest() {
        if (phase == Phase.REST) endRest()
    }

    fun addRest(seconds: Int) {
        if (phase == Phase.REST) restLeft += seconds
    }

    private fun endRest() {
        if (restThenNext) {
            restThenNext = false
            goTo(index + 1)
        } else {
            currentSet = (setsDone[index] + 1).coerceAtMost(item.sets)
            phase = Phase.READY
            reps = targetReps()
            side = 1
        }
    }

    private fun targetReps(): Int = items[index].amount

    fun goTo(i: Int) {
        if (i !in items.indices) return
        index = i
        restThenNext = false
        side = 1
        currentSet = (setsDone[i] + 1).coerceAtMost(items[i].sets)
        reps = items[i].amount
        phase = Phase.READY
        workLeft = 0
        restLeft = 0
    }

    fun next() {
        if (isLast) {
            phase = Phase.FINISHED
        } else {
            goTo(index + 1)
        }
    }

    fun previous() {
        if (index > 0) goTo(index - 1)
    }

    fun skipExercise() {
        if (setsDone[index] == 0) skipped[index] = true
        next()
    }

    /** Swaps the current exercise (e.g. for an easier variation). Only allowed before any set of it was done. */
    fun replaceCurrent(newItem: PlanItem) {
        if (setsDone[index] > 0 || phase == Phase.FINISHED) return
        items[index] = newItem
        reps = newItem.amount
        workLeft = 0
        restLeft = 0
        side = 1
        phase = Phase.READY
    }

    fun finishNow() {
        phase = Phase.FINISHED
    }

    fun results(): List<ExerciseResult> = items.mapIndexed { i, p ->
        ExerciseResult(p.exerciseId, p.sets, setsDone[i], best[i], skipped[i] && setsDone[i] == 0)
    }

    fun anythingDone(): Boolean = setsDone.any { it > 0 }
}
