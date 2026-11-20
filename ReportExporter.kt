package com.littlecare.babytracker.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.data.VaccineRecord
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ReportExporter {

    fun sharePediatricReport(
        context: Context,
        baby: BabyProfile,
        growth: List<GrowthEntry>,
        vaccines: List<VaccineRecord>,
        logs: List<ActivityLog>
    ) {
        val dob = LocalDate.ofEpochDay(baby.birthDateEpochDay)
        val fmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
        val timeFmt = DateTimeFormatter.ofPattern("dd MMM, hh:mm a").withZone(ZoneId.systemDefault())

        val sb = StringBuilder()
        sb.appendLine("LITTLECARE PEDIATRIC HEALTH SUMMARY")
        sb.appendLine("Baby Name,${baby.name}")
        sb.appendLine("Date of Birth,${dob.format(fmt)}")
        sb.appendLine("Gender,${baby.gender}")
        sb.appendLine("Birth Weight (kg),${baby.birthWeightKg}")
        sb.appendLine("Birth Height (cm),${baby.birthHeightCm}")
        sb.appendLine()

        sb.appendLine("=== WEEKLY GROWTH LOG (0-52 WEEKS) ===")
        sb.appendLine("Week,Date,Weight (kg),Height (cm),Head (cm)")
        growth.forEach { g ->
            val d = LocalDate.ofEpochDay(g.recordedEpochDay).format(fmt)
            sb.appendLine("Week ${g.weekNumber},$d,${g.weightKg},${g.heightCm},${g.headCircumferenceCm}")
        }
        sb.appendLine()

        sb.appendLine("=== INDIAN IMMUNIZATION SCHEDULE (MoHFW / IAP) ===")
        sb.appendLine("Age Group,Vaccine,Due Date,Status,Completed Date")
        vaccines.forEach { v ->
            val dueDate = dob.plusDays(v.dueOffsetDays.toLong()).format(fmt)
            val status = if (v.isCompleted) "Completed" else "Pending"
            val doneDate = v.completedEpochDay?.let { LocalDate.ofEpochDay(it).format(fmt) } ?: "-"
            sb.appendLine("${v.ageGroupLabel},"${v.name}",$dueDate,$status,$doneDate")
        }
        sb.appendLine()

        sb.appendLine("=== RECENT CARE LOGS (LAST 50) ===")
        sb.appendLine("Time,Category,Type,Volume (ml),Duration (min),Notes")
        logs.take(50).forEach { l ->
            val t = timeFmt.format(Instant.ofEpochMilli(l.timestampMillis))
            sb.appendLine(""$t",${l.category},${l.subType},${l.amountMl},${l.durationSeconds / 60},"${l.notes}"")
        }

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = baby.name.replace(Regex("[^A-Za-z0-9]"), "_")
        val file = File(exportDir, "LittleCare_${safeName}_Report.csv")
        file.writeText(sb.toString())

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "LittleCare Health Report - ${baby.name}")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Pediatric Report"))
    }
}
