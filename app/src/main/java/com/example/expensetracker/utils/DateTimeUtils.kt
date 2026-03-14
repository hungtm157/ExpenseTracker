package com.example.expensetracker.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {

    fun getHourMinute(isoDate: String?): String {
        if (isoDate.isNullOrEmpty()) return ""

        return try {
            val inputFormat =
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date = inputFormat.parse(isoDate)

            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date!!)
        } catch (e: Exception) {
            ""
        }
    }

    fun parseIsoDate(isoDate: String?): Date? {
        if (isoDate.isNullOrEmpty()) return null
        val formats = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd"
        )
        for (pattern in formats) {
            try {
                val format = SimpleDateFormat(pattern, Locale.getDefault())
                if (pattern.endsWith("'Z'")) {
                    format.timeZone = TimeZone.getTimeZone("UTC")
                }
                return format.parse(isoDate)
            } catch (e: Exception) {
                // Ignore and try next pattern
            }
        }
        return null
    }
}