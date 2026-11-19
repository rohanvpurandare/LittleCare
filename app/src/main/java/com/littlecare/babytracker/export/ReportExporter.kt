package com.littlecare.babytracker.export

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.ActivityType
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.data.VaccineRecord
import com.littlecare.babytracker.domain.WhoGrowthStandards
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

data class ExportResult(
    val fileName: String,
    val savedLocationMessage: String,
    val shareUri: Uri,
    val mimeType: String
)

object ReportExporter {

    private val dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy")
    private val dateTimeFmt = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val fileStampFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")

    suspend fun exportExcelWorkbook(
        context: Context,
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>
    ): ExportResult = withContext(Dispatchers.IO) {
        val safeName = profile.name.replace(Regex("[^A-Za-z0-9]"), "_").ifBlank { "Baby" }
        val stamp = LocalDateTime.now().format(fileStampFmt)
        val fileName = "LittleCare_${safeName}_Report_$stamp.xlsx"
        val mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"

        val bytes = buildXlsxBytes(profile, logs, growthEntries, vaccines)
        saveAndPrepareShare(context, fileName, mimeType, bytes)
    }

    suspend fun exportPdfReport(
        context: Context,
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>
    ): ExportResult = withContext(Dispatchers.IO) {
        val safeName = profile.name.replace(Regex("[^A-Za-z0-9]"), "_").ifBlank { "Baby" }
        val stamp = LocalDateTime.now().format(fileStampFmt)
        val fileName = "LittleCare_${safeName}_Report_$stamp.pdf"
        val mimeType = "application/pdf"

        val bytes = buildPdfBytes(profile, logs, growthEntries, vaccines)
        saveAndPrepareShare(context, fileName, mimeType, bytes)
    }

    fun launchShareSheet(context: Context, result: ExportResult) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = result.mimeType
            putExtra(Intent.EXTRA_STREAM, result.shareUri)
            putExtra(Intent.EXTRA_SUBJECT, result.fileName)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, "Share or Open ${result.fileName}").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun saveAndPrepareShare(
        context: Context,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): ExportResult {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val cacheFile = File(exportDir, fileName)
        FileOutputStream(cacheFile).use { it.write(bytes) }
        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )

        val locationMsg = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/LittleCare")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { it.write(bytes) }
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)
                    "Saved to Downloads/LittleCare/$fileName"
                } else {
                    "Ready to share ($fileName)"
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    "LittleCare"
                ).apply { mkdirs() }
                val outFile = File(downloadsDir, fileName)
                FileOutputStream(outFile).use { it.write(bytes) }
                "Saved to Downloads/LittleCare/$fileName"
            }
        } catch (_: Exception) {
            "Ready to open or share ($fileName)"
        }

        return ExportResult(
            fileName = fileName,
            savedLocationMessage = locationMsg,
            shareUri = shareUri,
            mimeType = mimeType
        )
    }

    // =========================================================================
    // ZERO-DEPENDENCY OPENXML (.XLSX) MULTI-SHEET GENERATOR
    // =========================================================================

    private fun buildXlsxBytes(
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>
    ): ByteArray {
        val dob = LocalDate.ofEpochDay(profile.birthDateEpochDay)
        val totalDays = ChronoUnit.DAYS.between(dob, LocalDate.now()).coerceAtLeast(0)
        val ageText = "${totalDays / 7} weeks, ${totalDays % 7} days"

        val totalFeeds = logs.count { it.type == ActivityType.FEED.name }
        val totalFeedMins = logs.filter { it.type == ActivityType.FEED.name }.sumOf { it.durationMinutes }
        val totalPee = logs.count { it.type == ActivityType.PEE.name }
        val totalPoop = logs.count { it.type == ActivityType.POOP.name }
        val givenVaccines = vaccines.count { it.isAdministered }

        val latestWeight = growthEntries.lastOrNull { it.weightKg != null }?.weightKg?.let { "$it kg" } ?: "Not logged"
        val latestHeight = growthEntries.lastOrNull { it.heightCm != null }?.heightCm?.let { "$it cm" } ?: "Not logged"

        // Sheet 1: Baby Summary
        val summaryRows = listOf(
            listOf(XCell("LITTLECARE PEDIATRIC & DAILY HEALTH REPORT", 1), XCell("", 1)),
            listOf(XCell("Generated On", 5), XCell(LocalDateTime.now().format(dateTimeFmt), 4)),
            listOf(XCell("", 0)),
            listOf(XCell("BABY PROFILE", 2), XCell("DETAILS", 2)),
            listOf(XCell("Baby Name", 5), XCell(profile.name, 4)),
            listOf(XCell("Birth Date", 5), XCell(dob.format(dateFmt), 4)),
            listOf(XCell("Birth Time", 5), XCell(profile.birthTimeText, 4)),
            listOf(XCell("Current Age", 5), XCell(ageText, 4)),
            listOf(XCell("", 0)),
            listOf(XCell("KEY HEALTH & ACTIVITY METRICS", 2), XCell("VALUE", 2)),
            listOf(XCell("Total Feeds Logged", 5), XCell("$totalFeeds feeds ($totalFeedMins mins total)", 4)),
            listOf(XCell("Total Wet Diapers (Urination)", 5), XCell(totalPee.toString(), 4)),
            listOf(XCell("Total Dirty Diapers (Poop)", 5), XCell(totalPoop.toString(), 4)),
            listOf(XCell("Latest Recorded Weight", 5), XCell(latestWeight, 4)),
            listOf(XCell("Latest Recorded Height", 5), XCell(latestHeight, 4)),
            listOf(XCell("Immunizations Completed", 5), XCell("$givenVaccines of ${vaccines.size} doses", 4))
        )
        val sheet1 = buildWorksheetXml(summaryRows, listOf(34, 38))

        // Sheet 2: Daily Activity Logs
        val activityRows = mutableListOf<List<XCell>>()
        activityRows.add(
            listOf(
                XCell("Date & Time", 2),
                XCell("Activity", 2),
                XCell("Type / Consistency", 2),
                XCell("Duration (Min)", 2),
                XCell("Notes", 2)
            )
        )
        logs.forEachIndexed { idx, log ->
            val style = if (idx % 2 == 1) 3 else 4
            val timeStr = Instant.ofEpochMilli(log.timestampMillis)
                .atZone(ZoneId.systemDefault())
                .format(dateTimeFmt)
            val activityName = when (log.type) {
                ActivityType.FEED.name -> "Feeding"
                ActivityType.POOP.name -> "Poop"
                else -> "Urination (Wet)"
            }
            val durationStr = if (log.type == ActivityType.FEED.name) log.durationMinutes.toString() else "-"
            activityRows.add(
                listOf(
                    XCell(timeStr, style),
                    XCell(activityName, style),
                    XCell(log.subType.ifBlank { "-" }, style),
                    XCell(durationStr, style),
                    XCell(log.note.ifBlank { "-" }, style)
                )
            )
        }
        val sheet2 = buildWorksheetXml(activityRows, listOf(24, 18, 22, 16, 36))

        // Sheet 3: Growth Tracker
        val growthRows = mutableListOf<List<XCell>>()
        growthRows.add(
            listOf(
                XCell("Age (Week)", 2),
                XCell("Recorded Date", 2),
                XCell("Weight (kg)", 2),
                XCell("WHO Median Weight (kg)", 2),
                XCell("Height (cm)", 2),
                XCell("WHO Median Height (cm)", 2)
            )
        )
        growthEntries.forEachIndexed { idx, entry ->
            val style = if (idx % 2 == 1) 3 else 4
            val recDate = LocalDate.ofEpochDay(entry.recordedDateEpochDay).format(dateFmt)
            val whoW = String.format(Locale.US, "%.1f", WhoGrowthStandards.getInterpolatedMedian(entry.weekNumber, true))
            val whoH = String.format(Locale.US, "%.1f", WhoGrowthStandards.getInterpolatedMedian(entry.weekNumber, false))
            growthRows.add(
                listOf(
                    XCell("Week ${entry.weekNumber}", style),
                    XCell(recDate, style),
                    XCell(entry.weightKg?.toString() ?: "-", style),
                    XCell(whoW, style),
                    XCell(entry.heightCm?.toString() ?: "-", style),
                    XCell(whoH, style)
                )
            )
        }
        val sheet3 = buildWorksheetXml(growthRows, listOf(14, 18, 16, 22, 16, 22))

        // Sheet 4: Indian MoHFW Immunization Schedule
        val vaccineRows = mutableListOf<List<XCell>>()
        vaccineRows.add(
            listOf(
                XCell("Stage", 2),
                XCell("Vaccine", 2),
                XCell("Due Date", 2),
                XCell("Status", 2),
                XCell("Administered Date", 2),
                XCell("Protection Details", 2)
            )
        )
        val todayEpoch = LocalDate.now().toEpochDay()
        vaccines.forEachIndexed { idx, v ->
            val style = if (idx % 2 == 1) 3 else 4
            val dueStr = LocalDate.ofEpochDay(v.dueDateEpochDay).format(dateFmt)
            val statusStr = when {
                v.isAdministered -> "Completed"
                v.dueDateEpochDay < todayEpoch -> "Overdue"
                else -> "Upcoming"
            }
            val givenStr = v.administeredDateEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFmt) } ?: "-"
            vaccineRows.add(
                listOf(
                    XCell(v.stageLabel, style),
                    XCell(v.name, style),
                    XCell(dueStr, style),
                    XCell(statusStr, style),
                    XCell(givenStr, style),
                    XCell(v.description, style)
                )
            )
        }
        val sheet4 = buildWorksheetXml(vaccineRows, listOf(16, 26, 16, 14, 18, 44))

        val baos = ByteArrayOutputStream()
        ZipOutputStream(baos).use { zip ->
            fun addEntry(path: String, xml: String) {
                zip.putNextEntry(ZipEntry(path))
                zip.write(xml.toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
            }

            addEntry("[Content_Types].xml", CONTENT_TYPES_XML)
            addEntry("_rels/.rels", ROOT_RELS_XML)
            addEntry("xl/workbook.xml", WORKBOOK_XML)
            addEntry("xl/_rels/workbook.xml.rels", WORKBOOK_RELS_XML)
            addEntry("xl/styles.xml", STYLES_XML)
            addEntry("xl/worksheets/sheet1.xml", sheet1)
            addEntry("xl/worksheets/sheet2.xml", sheet2)
            addEntry("xl/worksheets/sheet3.xml", sheet3)
            addEntry("xl/worksheets/sheet4.xml", sheet4)
        }
        return baos.toByteArray()
    }

    private data class XCell(val value: String, val styleIndex: Int)

    private fun buildWorksheetXml(rows: List<List<XCell>>, colWidths: List<Int>): String {
        val sb = StringBuilder()
        sb.append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
        sb.append("""<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">""")
        sb.append("<cols>")
        colWidths.forEachIndexed { index, width ->
            val colIdx = index + 1
            sb.append("""<col min="$colIdx" max="$colIdx" width="$width" customWidth="1"/>""")
        }
        sb.append("</cols>")
        sb.append("<sheetData>")
        rows.forEachIndexed { rIdx, row ->
            val rowNum = rIdx + 1
            val heightAttr = if (rIdx == 0) """ ht="24" customHeight="1"""" else """ ht="20" customHeight="1""""
            sb.append("""<row r="$rowNum"$heightAttr>""")
            row.forEachIndexed { cIdx, cell ->
                val colLetter = ('A' + cIdx)
                val ref = "$colLetter$rowNum"
                val escaped = escapeXml(cell.value)
                sb.append("""<c r="$ref" s="${cell.styleIndex}" t="inlineStr"><is><t>$escaped</t></is></c>""")
            }
            sb.append("</row>")
        }
        sb.append("</sheetData></worksheet>")
        return sb.toString()
    }

    private fun escapeXml(raw: String): String {
        return raw
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    // =========================================================================
    // MULTI-PAGE VECTOR PDF PEDIATRIC REPORT GENERATOR
    // =========================================================================

    private fun buildPdfBytes(
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>
    ): ByteArray {
        val doc = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 36f
        val contentWidth = pageWidth - (margin * 2)

        var pageNumber = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
        var canvas: Canvas = page.canvas
        var y = margin

        val headerBgPaint = Paint().apply { color = Color.parseColor("#0F172A"); style = Paint.Style.FILL }
        val accentGoldPaint = Paint().apply { color = Color.parseColor("#D97706"); style = Paint.Style.FILL }
        val cardBgPaint = Paint().apply { color = Color.parseColor("#F8FAFC"); style = Paint.Style.FILL }
        val rowAltPaint = Paint().apply { color = Color.parseColor("#F1F5F9"); style = Paint.Style.FILL }
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            style = Paint.Style.STROKE
            strokeWidth = 1f
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val subHeaderWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            textSize = 10.5f
        }
        val sectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tableHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 9.5f
        }
        val bodyBoldPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val mutedPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 8.5f
        }

        fun startNewPage() {
            doc.finishPage(page)
            pageNumber++
            page = doc.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create())
            canvas = page.canvas
            y = margin
            canvas.drawText("LittleCare Pediatric Report • ${profile.name} (Page $pageNumber)", margin, y + 10f, mutedPaint)
            y += 24f
        }

        fun ensureSpace(needed: Float) {
            if (y + needed > pageHeight - margin) {
                startNewPage()
            }
        }

        val dob = LocalDate.ofEpochDay(profile.birthDateEpochDay)
        val totalDays = ChronoUnit.DAYS.between(dob, LocalDate.now()).coerceAtLeast(0)
        val ageText = "${totalDays / 7}w ${totalDays % 7}d old"

        canvas.drawRoundRect(RectF(margin, y, margin + contentWidth, y + 74f), 10f, 10f, headerBgPaint)
        canvas.drawRect(margin, y + 70f, margin + contentWidth, y + 74f, accentGoldPaint)
        canvas.drawText("LITTLECARE • BABY HEALTH & ACTIVITY REPORT", margin + 16f, y + 26f, titlePaint)
        canvas.drawText(
            "Baby: ${profile.name}   |   DOB: ${dob.format(dateFmt)} (${profile.birthTimeText})   |   Age: $ageText",
            margin + 16f,
            y + 46f,
            subHeaderWhitePaint
        )
        canvas.drawText(
            "Generated on ${LocalDateTime.now().format(dateTimeFmt)}",
            margin + 16f,
            y + 61f,
            subHeaderWhitePaint
        )
        y += 90f

        val feedCount = logs.count { it.type == ActivityType.FEED.name }
        val peeCount = logs.count { it.type == ActivityType.PEE.name }
        val poopCount = logs.count { it.type == ActivityType.POOP.name }
        val givenVaccines = vaccines.count { it.isAdministered }
        val latestW = growthEntries.lastOrNull { it.weightKg != null }?.weightKg?.let { "$it kg" } ?: "—"
        val latestH = growthEntries.lastOrNull { it.heightCm != null }?.heightCm?.let { "$it cm" } ?: "—"

        canvas.drawRoundRect(RectF(margin, y, margin + contentWidth, y + 54f), 8f, 8f, cardBgPaint)
        canvas.drawRoundRect(RectF(margin, y, margin + contentWidth, y + 54f), 8f, 8f, borderPaint)

        val kpis = listOf(
            "Feeds" to feedCount.toString(),
            "Wet (Pee)" to peeCount.toString(),
            "Dirty (Poop)" to poopCount.toString(),
            "Latest Weight" to latestW,
            "Latest Height" to latestH,
            "Vaccines" to "$givenVaccines/${vaccines.size}"
        )
        val colW = contentWidth / kpis.size
        kpis.forEachIndexed { i, (label, value) ->
            val x = margin + (i * colW) + 10f
            canvas.drawText(label, x, y + 20f, mutedPaint)
            canvas.drawText(value, x, y + 39f, bodyBoldPaint)
        }
        y += 74f

        // Section 1: Immunization Schedule
        ensureSpace(60f)
        canvas.drawText("1. Immunization Schedule (Indian MoHFW & NMC Guidelines)", margin, y, sectionPaint)
        y += 10f

        fun drawTableHeader(cols: List<Pair<String, Float>>) {
            ensureSpace(24f)
            canvas.drawRect(margin, y, margin + contentWidth, y + 20f, headerBgPaint)
            var cx = margin + 8f
            cols.forEach { (title, width) ->
                canvas.drawText(title, cx, y + 13.5f, tableHeaderPaint)
                cx += width
            }
            y += 20f
        }

        val vCols = listOf("Stage" to 78f, "Vaccine" to 175f, "Due Date" to 92f, "Status" to 85f, "Given Date" to 93f)
        drawTableHeader(vCols)

        val todayEpoch = LocalDate.now().toEpochDay()
        vaccines.forEachIndexed { idx, v ->
            ensureSpace(19f)
            if (idx % 2 == 1) {
                canvas.drawRect(margin, y, margin + contentWidth, y + 18f, rowAltPaint)
            }
            val dueStr = LocalDate.ofEpochDay(v.dueDateEpochDay).format(dateFmt)
            val statusStr = when {
                v.isAdministered -> "Completed"
                v.dueDateEpochDay < todayEpoch -> "Overdue"
                else -> "Upcoming"
            }
            val givenStr = v.administeredDateEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFmt) } ?: "—"
            val rowValues = listOf(v.stageLabel, v.name, dueStr, statusStr, givenStr)

            var cx = margin + 8f
            rowValues.forEachIndexed { cIdx, text ->
                canvas.drawText(text.take(28), cx, y + 12.5f, if (cIdx == 1) bodyBoldPaint else bodyPaint)
                cx += vCols[cIdx].second
            }
            y += 18f
        }
        y += 20f

        // Section 2: Growth Measurements
        ensureSpace(60f)
        canvas.drawText("2. Growth Measurements (0–52 Weeks)", margin, y, sectionPaint)
        y += 10f

        val gCols = listOf("Week" to 75f, "Date" to 110f, "Weight" to 110f, "Height" to 110f, "WHO Median" to 118f)
        drawTableHeader(gCols)

        if (growthEntries.isEmpty()) {
            ensureSpace(22f)
            canvas.drawText("No weight or height measurements recorded yet.", margin + 8f, y + 14f, mutedPaint)
            y += 24f
        } else {
            growthEntries.forEachIndexed { idx, g ->
                ensureSpace(19f)
                if (idx % 2 == 1) {
                    canvas.drawRect(margin, y, margin + contentWidth, y + 18f, rowAltPaint)
                }
                val dStr = LocalDate.ofEpochDay(g.recordedDateEpochDay).format(dateFmt)
                val wStr = g.weightKg?.let { "$it kg" } ?: "—"
                val hStr = g.heightCm?.let { "$it cm" } ?: "—"
                val medW = String.format(Locale.US, "%.1f kg", WhoGrowthStandards.getInterpolatedMedian(g.weekNumber, true))
                val vals = listOf("Week ${g.weekNumber}", dStr, wStr, hStr, medW)
                var cx = margin + 8f
                vals.forEachIndexed { cIdx, text ->
                    canvas.drawText(text, cx, y + 12.5f, bodyPaint)
                    cx += gCols[cIdx].second
                }
                y += 18f
            }
        }
        y += 20f

        // Section 3: Recent Activity Logs
        ensureSpace(60f)
        canvas.drawText("3. Daily Activity Logs (Latest ${logs.take(60).size} Entries)", margin, y, sectionPaint)
        y += 10f

        val aCols = listOf("Date & Time" to 145f, "Activity" to 95f, "Details" to 125f, "Notes" to 158f)
        drawTableHeader(aCols)

        if (logs.isEmpty()) {
            ensureSpace(22f)
            canvas.drawText("No daily activity logs recorded yet.", margin + 8f, y + 14f, mutedPaint)
            y += 24f
        } else {
            logs.take(60).forEachIndexed { idx, log ->
                ensureSpace(19f)
                if (idx % 2 == 1) {
                    canvas.drawRect(margin, y, margin + contentWidth, y + 18f, rowAltPaint)
                }
                val tStr = Instant.ofEpochMilli(log.timestampMillis)
                    .atZone(ZoneId.systemDefault())
                    .format(dateTimeFmt)
                val act = when (log.type) {
                    ActivityType.FEED.name -> "Feeding"
                    ActivityType.POOP.name -> "Poop"
                    else -> "Urination"
                }
                val det = when (log.type) {
                    ActivityType.FEED.name -> "${log.subType} (${log.durationMinutes}m)"
                    ActivityType.POOP.name -> log.subType
                    else -> "Wet Diaper"
                }
                val noteStr = log.note.ifBlank { "—" }.take(28)
                val vals = listOf(tStr, act, det, noteStr)
                var cx = margin + 8f
                vals.forEachIndexed { cIdx, text ->
                    canvas.drawText(text, cx, y + 12.5f, if (cIdx == 1) bodyBoldPaint else bodyPaint)
                    cx += aCols[cIdx].second
                }
                y += 18f
            }
        }

        doc.finishPage(page)

        val out = ByteArrayOutputStream()
        doc.writeTo(out)
        doc.close()
        return out.toByteArray()
    }

    private val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
  <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
  <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
  <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
</Types>"""

    private val ROOT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <sheets>
    <sheet name="Summary" sheetId="1" r:id="rId1"/>
    <sheet name="Daily Logs" sheetId="2" r:id="rId2"/>
    <sheet name="Growth Tracker" sheetId="3" r:id="rId3"/>
    <sheet name="Vaccines (MoHFW)" sheetId="4" r:id="rId4"/>
  </sheets>
</workbook>"""

    private val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
  <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
  <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
  <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
  <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private val STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
  <fonts count="4">
    <font><sz val="11"/><color rgb="FF1E293B"/><name val="Calibri"/></font>
    <font><b/><sz val="13"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
    <font><b/><sz val="11"/><color rgb="FF0F172A"/><name val="Calibri"/></font>
  </fonts>
  <fills count="5">
    <fill><patternFill patternType="none"/></fill>
    <fill><patternFill patternType="gray125"/></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF0F172A"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FF1E293B"/></patternFill></fill>
    <fill><patternFill patternType="solid"><fgColor rgb="FFF8FAFC"/></patternFill></fill>
  </fills>
  <borders count="2">
    <border/>
    <border>
      <left style="thin"><color rgb="FFCBD5E1"/></left>
      <right style="thin"><color rgb="FFCBD5E1"/></right>
      <top style="thin"><color rgb="FFCBD5E1"/></top>
      <bottom style="thin"><color rgb="FFCBD5E1"/></bottom>
    </border>
  </borders>
  <cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>
  <cellXfs count="6">
    <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
    <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1" applyAlignment="1"><alignment vertical="center"/></xf>
    <xf numFmtId="0" fontId="2" fillId="3" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
    <xf numFmtId="0" fontId="0" fillId="4" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
    <xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
    <xf numFmtId="0" fontId="3" fillId="0" borderId="1" xfId="0" applyFont="1" applyBorder="1" applyAlignment="1"><alignment vertical="center"/></xf>
  </cellXfs>
</styleSheet>"""
}
