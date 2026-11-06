package com.calisthenics.coach.logic

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Dates {
    private fun fmt() = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    fun key(cal: Calendar): String = fmt().format(cal.time)

    fun todayKey(): String = key(Calendar.getInstance())

    fun keyFromMillis(ms: Long): String {
        val c = Calendar.getInstance()
        c.timeInMillis = ms
        return key(c)
    }

    fun parse(key: String): Calendar {
        val c = Calendar.getInstance()
        c.time = fmt().parse(key) ?: c.time
        c.set(Calendar.HOUR_OF_DAY, 12)
        return c
    }

    /** Saturday-based day index: Sat=0, Sun=1, ... Fri=6. */
    fun daySat0(cal: Calendar): Int = cal.get(Calendar.DAY_OF_WEEK) % 7

    fun todaySat0(): Int = daySat0(Calendar.getInstance())

    /** Key of the Saturday that starts the week containing [cal]. */
    fun weekStartKey(cal: Calendar): String {
        val c = cal.clone() as Calendar
        c.add(Calendar.DAY_OF_YEAR, -daySat0(c))
        return key(c)
    }

    fun addDays(key: String, days: Int): String {
        val c = parse(key)
        c.add(Calendar.DAY_OF_YEAR, days)
        return key(c)
    }

    fun daysBetween(from: String, to: String): Int {
        val a = parse(from).timeInMillis
        val b = parse(to).timeInMillis
        return Math.round((b - a) / 86_400_000.0).toInt()
    }

    fun formatShort(key: String): String {
        val c = parse(key)
        return "${c.get(Calendar.DAY_OF_MONTH)}/${c.get(Calendar.MONTH) + 1}"
    }

    fun monthName(month0: Int): String = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )[month0.coerceIn(0, 11)]
}
