package com.calisthenics.coach.logic

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class AppViewModel(app: Application) : AndroidViewModel(app) {

    var state by mutableStateOf(AppState())
        private set

    var loaded by mutableStateOf(false)
        private set

    /** The workout currently being performed (kept here so it survives screen navigation). */
    var session by mutableStateOf<WorkoutSession?>(null)
        private set

    private val file = File(app.filesDir, "state.json")
    private val tmp = File(app.filesDir, "state.json.tmp")
    private val lock = Mutex()

    init {
        viewModelScope.launch {
            val loadedState = withContext(Dispatchers.IO) { readFromDisk() }
            state = loadedState
            loaded = true
        }
    }

    private fun readFromDisk(): AppState {
        return try {
            if (file.exists()) StateJson.fromJson(file.readText()) ?: AppState() else AppState()
        } catch (e: Exception) {
            AppState()
        }
    }

    private fun persist() {
        val snapshot = StateJson.toJson(state)
        viewModelScope.launch(Dispatchers.IO) {
            lock.withLock {
                try {
                    tmp.writeText(snapshot)
                    if (!tmp.renameTo(file)) {
                        file.writeText(snapshot)
                        tmp.delete()
                    }
                } catch (e: Exception) {
                    // Disk problems must never crash a workout.
                }
            }
        }
    }

    fun update(block: (AppState) -> AppState) {
        state = block(state)
        persist()
    }

    // ----------------------------------------------------------------- plan

    fun ensurePlan() {
        val today = Dates.todayKey()
        if (state.plan.isEmpty() || state.planDate != today) {
            val readiness = if (state.planDate == today) state.planReadiness else 0
            val plan = Generator.generate(state, readiness)
            update { it.copy(plan = plan, planDate = today, planReadiness = readiness) }
        }
    }

    fun regenerate(readiness: Int) {
        val plan = Generator.generate(state, readiness)
        update { it.copy(plan = plan, planDate = Dates.todayKey(), planReadiness = readiness) }
    }

    fun replaceExercise(index: Int, newId: String) {
        update { it.copy(plan = Generator.replace(it, it.plan, index, newId)) }
    }

    // ----------------------------------------------------------------- session

    fun startSession() {
        if (state.plan.isEmpty()) return
        session = WorkoutSession(state.plan)
    }

    fun discardSession() {
        session = null
    }

    fun saveSession(feedback: Int, pain: Boolean, fatigue: Boolean, plankSeconds: Int?) {
        val s = session ?: return
        val now = System.currentTimeMillis()
        val record = WorkoutRecord(
            id = now,
            dateKey = Dates.keyFromMillis(now),
            stage = state.stage,
            durationSec = s.elapsed,
            results = s.results(),
            feedback = feedback,
            pain = pain,
            fatigue = fatigue
        )
        update { Progress.applyWorkout(it, record, plankSeconds) }
        session = null
    }

    // ----------------------------------------------------------------- settings

    fun setTheme(v: Int) = update { it.copy(theme = v) }
    fun setSound(v: Boolean) = update { it.copy(sound = v) }
    fun setVibration(v: Boolean) = update { it.copy(vibration = v) }

    fun setSessionMinutes(v: Int) = update { it.copy(sessionMinutes = v, plan = emptyList(), planDate = "") }

    fun toggleTrainingDay(day: Int) = update {
        val set = it.trainingDays.toMutableSet()
        if (!set.add(day)) set.remove(day)
        // Never fewer than 2 or more than 5 training days (avoids too little or excessive volume).
        if (set.size in 2..5) it.copy(trainingDays = set.sorted()) else it
    }

    fun finishOnboarding(minutes: Int) = update { it.copy(onboarded = true, sessionMinutes = minutes) }

    fun advanceStage() = update { Progress.advance(it) }
    fun toggleStageCheck(key: String) = update { Progress.toggleStageCheck(it, key) }
    fun toggleSkillTest(key: String) = update { Progress.toggleSkillTest(it, key) }
    fun setSkillLevel(skillId: String, level: Int) = update { Progress.setSkillLevel(it, skillId, level) }

    fun addPlank(seconds: Int) {
        if (seconds <= 0) return
        update {
            it.copy(
                plankHistory = it.plankHistory + PlankEntry(Dates.todayKey(), seconds),
                records = it.records + ("plank" to maxOf(it.records["plank"] ?: 0, seconds))
            )
        }
    }

    // ----------------------------------------------------------------- backup / restore

    fun exportText(): String = StateJson.toJson(state)

    /** Returns true if the text was a valid backup and has been restored. */
    fun importText(text: String): Boolean {
        val restored = StateJson.fromJson(text) ?: return false
        session = null
        state = restored.copy(onboarded = true, plan = emptyList(), planDate = "")
        persist()
        return true
    }

    fun resetAll() {
        session = null
        state = AppState()
        persist()
    }
}
