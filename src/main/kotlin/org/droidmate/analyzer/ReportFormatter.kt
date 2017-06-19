package org.droidmate.analyzer

import org.droidmate.analyzer.api.IApi
import java.text.SimpleDateFormat
import java.util.*

/**
 * Format template for the report
 */
object ReportFormatter {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    internal fun formatApiList(apis: List<IApi>): String {
        val b = StringBuilder()
        apis.forEach { p -> b.append(String.format("\t%s\n", p.toString())) }

        return b.toString()
    }

    internal fun formatDate(date: Date): String {
        return dateFormat.format(date)
    }
}
