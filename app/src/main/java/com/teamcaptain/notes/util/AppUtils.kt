package com.teamcaptain.notes.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

/** Small, dependency-free helpers for ids, dates and input validation. */
object Ids {
    fun newId(): String = UUID.randomUUID().toString()
}

object DateUtils {

    private val isoDate: SimpleDateFormat
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }

    private val isoTime: SimpleDateFormat
        get() = SimpleDateFormat("HH:mm", Locale.US).apply { isLenient = false }

    /** Today as YYYY-MM-DD from the device clock. */
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    /** today + [days] as YYYY-MM-DD. */
    fun todayPlus(days: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun nowIsoTimestamp(): String =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date())

    fun isValidDate(value: String): Boolean {
        if (value.isBlank()) return false
        return runCatching { isoDate.parse(value) }.getOrNull() != null &&
            value.matches(Regex("""\d{4}-\d{2}-\d{2}"""))
    }

    fun isValidTime(value: String): Boolean {
        if (value.isBlank()) return false
        return runCatching { isoTime.parse(value) }.getOrNull() != null &&
            value.matches(Regex("""\d{2}:\d{2}"""))
    }

    /** Returns true when a <= b as calendar dates. Invalid input returns true (non-blocking). */
    fun dateNotAfter(a: String, b: String): Boolean {
        if (a.isBlank() || b.isBlank()) return true
        val da = runCatching { isoDate.parse(a) }.getOrNull() ?: return true
        val db = runCatching { isoDate.parse(b) }.getOrNull() ?: return true
        return !da.after(db)
    }

    /** Formats an ISO-8601 UTC instant (e.g. 2026-07-30T18:30:00Z) into date + time strings. */
    fun splitUtcDate(utc: String?): Pair<String, String> {
        if (utc.isNullOrBlank()) return "" to ""
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            parser.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val cleaned = utc.substringBefore("Z").substringBefore("+").take(19)
            val parsed = parser.parse(cleaned) ?: return "" to ""
            // Convert to the device local zone for display.
            val dateOut = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val timeOut = SimpleDateFormat("HH:mm", Locale.US)
            dateOut.format(parsed) to timeOut.format(parsed)
        }.getOrElse { "" to "" }
    }
}

object Validation {
    fun shirtNumberError(raw: String): String? {
        if (raw.isBlank()) return null
        val n = raw.toIntOrNull() ?: return "Shirt number must be a number (1-99) or empty."
        if (n < 1 || n > 99) return "Shirt number must be between 1 and 99."
        return null
    }
}
