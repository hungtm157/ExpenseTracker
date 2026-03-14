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
}