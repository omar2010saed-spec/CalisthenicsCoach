package com.calisthenics.coach.logic

import org.json.JSONArray
import org.json.JSONObject

/** Hand-written JSON codec so the app needs no serialization library and backups are plain readable files. */
object StateJson {

    private const val FORMAT = "calisthenics-coach-backup"
    private const val VERSION = 1

    private fun intMap(m: Map<String, Int>): JSONObject {
        val o = JSONObject()
        m.forEach { (k, v) -> o.put(k, v) }
        return o
    }

    private fun readIntMap(o: JSONObject?): Map<String, Int> {
        if (o == null) return emptyMap()
        val out = LinkedHashMap<String, Int>()
        val it = o.keys()
        while (it.hasNext()) {
            val k = it.next()
            out[k] = o.optInt(k)
        }
        return out
    }

    private fun strArr(c: Collection<String>): JSONArray {
        val a = JSONArray()
        c.forEach { a.put(it) }
        return a
    }

    private fun readStrList(a: JSONArray?): List<String> {
        if (a == null) return emptyList()
        return (0 until a.length()).map { a.optString(it) }
    }

    private fun planToJson(p: PlanItem) = JSONObject()
        .put("id", p.exerciseId).put("sets", p.sets).put("amount", p.amount).put("rest", p.rest)

    private fun planFromJson(o: JSONObject) = PlanItem(
        o.optString("id"), o.optInt("sets", 1), o.optInt("amount", 8), o.optInt("rest", 60)
    )

    private fun resultToJson(r: ExerciseResult) = JSONObject()
        .put("id", r.id).put("planned", r.setsPlanned).put("done", r.setsDone)
        .put("best", r.best).put("skipped", r.skipped)

    private fun resultFromJson(o: JSONObject) = ExerciseResult(
        o.optString("id"), o.optInt("planned"), o.optInt("done"), o.optInt("best"), o.optBoolean("skipped")
    )

    private fun workoutToJson(w: WorkoutRecord): JSONObject {
        val res = JSONArray()
        w.results.forEach { res.put(resultToJson(it)) }
        return JSONObject()
            .put("id", w.id).put("date", w.dateKey).put("stage", w.stage)
            .put("duration", w.durationSec).put("feedback", w.feedback)
            .put("pain", w.pain).put("fatigue", w.fatigue).put("results", res)
    }

    private fun workoutFromJson(o: JSONObject): WorkoutRecord {
        val arr = o.optJSONArray("results") ?: JSONArray()
        return WorkoutRecord(
            id = o.optLong("id"),
            dateKey = o.optString("date"),
            stage = o.optInt("stage", 1),
            durationSec = o.optInt("duration"),
            results = (0 until arr.length()).map { resultFromJson(arr.getJSONObject(it)) },
            feedback = o.optInt("feedback", 1),
            pain = o.optBoolean("pain"),
            fatigue = o.optBoolean("fatigue")
        )
    }

    fun toJson(s: AppState): String {
        val root = JSONObject()
        root.put("format", FORMAT)
        root.put("version", VERSION)
        root.put("onboarded", s.onboarded)
        root.put("stage", s.stage)
        root.put("theme", s.theme)
        root.put("sound", s.sound)
        root.put("vibration", s.vibration)
        root.put("sessionMinutes", s.sessionMinutes)
        root.put("trainingDays", JSONArray(s.trainingDays))
        val w = JSONArray()
        s.workouts.forEach { w.put(workoutToJson(it)) }
        root.put("workouts", w)
        root.put("records", intMap(s.records))
        root.put("targets", intMap(s.targets))
        root.put("slotIdx", intMap(s.slotIdx))
        val ph = JSONArray()
        s.plankHistory.forEach { ph.put(JSONObject().put("date", it.dateKey).put("sec", it.seconds)) }
        root.put("plankHistory", ph)
        root.put("skillProgress", intMap(s.skillProgress))
        root.put("skillTests", strArr(s.skillTests))
        root.put("stageChecks", strArr(s.stageChecks))
        root.put("painDates", strArr(s.painDates))
        val plan = JSONArray()
        s.plan.forEach { plan.put(planToJson(it)) }
        root.put("plan", plan)
        root.put("planDate", s.planDate)
        root.put("planReadiness", s.planReadiness)
        return root.toString()
    }

    /** Returns null if the text is not a valid backup of this app. */
    fun fromJson(text: String): AppState? {
        return try {
            val o = JSONObject(text)
            if (o.optString("format") != FORMAT) return null
            val days = o.optJSONArray("trainingDays")
            val wArr = o.optJSONArray("workouts") ?: JSONArray()
            val phArr = o.optJSONArray("plankHistory") ?: JSONArray()
            val planArr = o.optJSONArray("plan") ?: JSONArray()
            AppState(
                onboarded = o.optBoolean("onboarded", true),
                stage = o.optInt("stage", 1).coerceIn(1, 6),
                theme = o.optInt("theme", 0).coerceIn(0, 2),
                sound = o.optBoolean("sound", true),
                vibration = o.optBoolean("vibration", true),
                sessionMinutes = o.optInt("sessionMinutes", 45).coerceIn(20, 60),
                trainingDays = if (days == null) listOf(0, 2, 4) else (0 until days.length()).map { days.getInt(it) },
                workouts = (0 until wArr.length()).map { workoutFromJson(wArr.getJSONObject(it)) },
                records = readIntMap(o.optJSONObject("records")),
                targets = readIntMap(o.optJSONObject("targets")),
                slotIdx = readIntMap(o.optJSONObject("slotIdx")),
                plankHistory = (0 until phArr.length()).map {
                    val p = phArr.getJSONObject(it)
                    PlankEntry(p.optString("date"), p.optInt("sec"))
                },
                skillProgress = readIntMap(o.optJSONObject("skillProgress")),
                skillTests = readStrList(o.optJSONArray("skillTests")).toSet(),
                stageChecks = readStrList(o.optJSONArray("stageChecks")).toSet(),
                painDates = readStrList(o.optJSONArray("painDates")),
                plan = (0 until planArr.length()).map { planFromJson(planArr.getJSONObject(it)) },
                planDate = o.optString("planDate"),
                planReadiness = o.optInt("planReadiness", 0)
            )
        } catch (e: Exception) {
            null
        }
    }
}
