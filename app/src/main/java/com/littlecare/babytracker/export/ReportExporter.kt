package com.littlecare.babytracker.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.littlecare.babytracker.data.ActivityLog
import com.littlecare.babytracker.data.ActivityType
import com.littlecare.babytracker.data.BabyProfile
import com.littlecare.babytracker.data.GrowthEntry
import com.littlecare.babytracker.data.VaccineRecord
import com.littlecare.babytracker.domain.WhoGrowthStandards
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

enum class ExportFormat {
    EXCEL
}

data class ExportResult(
    val file: File,
    val mimeType: String
)

object ReportExporter {

    fun generateAndShareReport(
        context: Context,
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>,
        format: ExportFormat = ExportFormat.EXCEL
    ) {
        val result = generateReportFile(context, profile, logs, growthEntries, vaccines, format)
        shareFile(context, result)
    }

    private fun generateReportFile(
        context: Context,
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>,
        format: ExportFormat
    ): ExportResult {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss", Locale.US)
        val timestamp = dateFmt.format(java.time.LocalDateTime.now())

        return when (format) {
            ExportFormat.EXCEL -> {
                val fileName = "LittleCare_Report_${profile.name}_$timestamp.xlsx"
                val file = File(exportDir, fileName)
                val excelBytes = buildMinimalXlsx(profile, logs, growthEntries, vaccines)
                FileOutputStream(file).use { it.write(excelBytes) }
                ExportResult(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            }
        }
    }

    private fun shareFile(context: Context, result: ExportResult) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            result.file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = result.mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Exported Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun buildMinimalXlsx(
        profile: BabyProfile,
        logs: List<ActivityLog>,
        growthEntries: List<GrowthEntry>,
        vaccines: List<VaccineRecord>
    ): ByteArray {
        val dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
        val timeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.US)

        val profileRows = listOf(
            listOf(xCell("Baby Name", 1), xCell(profile.name, 2)),
            listOf(xCell("Birth Date", 1), xCell(profile.birthDateEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFmt) } ?: "-", 2)),
            listOf(xCell("Gender", 1), xCell(profile.gender.name, 2)),
            listOf(xCell("Blood Group", 1), xCell(profile.bloodGroup.ifBlank { "-" }, 2)),
            listOf(xCell("Allergies", 1), xCell(profile.allergies.ifBlank { "None" }, 2)),
            listOf(xCell("Pediatrician Contact", 1), xCell(profile.pediatricianContact.ifBlank { "-" }, 2))
        )
        val sheet1 = buildWorksheetXml(profileRows, listOf(20, 30))

        val activityRows = mutableListOf<List<XCell>>()
        activityRows.add(
            listOf(
                xCell("Timestamp", 2),
                xCell("Activity Type", 2),
                xCell("Sub-Type", 2),
                xCell("Duration (mins)", 2),
                xCell("Notes", 2)
            )
        )

        val totalFeeds = logs.count { it.category == ActivityType.FEED.name }
        val totalFeedMins = logs.filter { it.category == ActivityType.FEED.name }.sumOf { (it.durationSeconds ?: 0) / 60 }
        val totalPee = logs.count { it.category == ActivityType.PEE.name }
        val totalPoop = logs.count { it.category == ActivityType.POOP.name }
        val givenVaccines = vaccines.count { it.isCompleted }

        val summaryRows = listOf(
            listOf(xCell("Total Feed Events", 1), xCell(totalFeeds.toString(), 2)),
            listOf(xCell("Total Feed Duration (mins)", 1), xCell(totalFeedMins.toString(), 2)),
            listOf(xCell("Total Pee (Wet Diapers)", 1), xCell(totalPee.toString(), 2)),
            listOf(xCell("Total Poop Events", 1), xCell(totalPoop.toString(), 2)),
            listOf(xCell("Vaccines Administered", 1), xCell("$givenVaccines / ${vaccines.size}", 2))
        )

        logs.forEachIndexed { idx, log ->
            val style = if (idx % 2 == 1) 3 else 4
            val timeStr = java.time.Instant.ofEpochMilli(log.timestampMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .format(timeFmt)

            val activityName = when (log.category) {
                ActivityType.FEED.name -> "Feeding"
                ActivityType.POOP.name -> "Poop"
                else -> "Urination (Wet)"
            }
            val durationStr = if (log.category == ActivityType.FEED.name) ((log.durationSeconds ?: 0) / 60).toString() else "-"

            activityRows.add(
                listOf(
                    xCell(timeStr, style),
                    xCell(activityName, style),
                    xCell(log.subType ?: "-", style),
                    xCell(durationStr, style),
                    xCell(log.notes ?: "-", style)
                )
            )
        }
        val sheet2 = buildWorksheetXml(activityRows, listOf(24, 18, 22, 16, 36))

        val growthRows = mutableListOf<List<XCell>>()
        growthRows.add(
            listOf(
                xCell("Age (Week)", 2),
                xCell("Recorded Date", 2),
                xCell("Weight (kg)", 2),
                xCell("WHO Median Weight (kg)", 2),
                xCell("Height (cm)", 2),
                xCell("WHO Median Height (cm)", 2)
            )
        )

        growthEntries.forEachIndexed { idx, entry ->
            val style = if (idx % 2 == 1) 3 else 4
            val recDate = LocalDate.ofEpochDay(entry.recordedDateEpochDay).format(dateFmt)
            val whoW = String.format(Locale.US, "%.1f", WhoGrowthStandards.getInterpolatedMedian(entry.weekNumber, true))
            val whoH = String.format(Locale.US, "%.1f", WhoGrowthStandards.getInterpolatedMedian(entry.weekNumber, false))
            growthRows.add(
                listOf(
                    xCell("Week ${entry.weekNumber}", style),
                    xCell(recDate, style),
                    xCell(entry.weightKg?.toString() ?: "-", style),
                    xCell(whoW, style),
                    xCell(entry.heightCm?.toString() ?: "-", style),
                    xCell(whoH, style)
                )
            )
        }
        val sheet3 = buildWorksheetXml(growthRows, listOf(14, 18, 16, 22, 16, 22))

        val vaccineRows = mutableListOf<List<XCell>>()
        vaccineRows.add(
            listOf(
                xCell("Stage", 2),
                xCell("Vaccine", 2),
                xCell("Due Date", 2),
                xCell("Status", 2),
                xCell("Administered Date", 2),
                xCell("Protection Details", 2)
            )
        )

        val todayEpoch = LocalDate.now().toEpochDay()
        vaccines.forEachIndexed { idx, v ->
            val style = if (idx % 2 == 1) 3 else 4
            val dueStr = LocalDate.ofEpochDay(v.dueOffsetDays.toLong()).format(dateFmt)
            val statusStr = when {
                v.isCompleted -> "Completed"
                v.dueOffsetDays.toLong() < todayEpoch -> "Overdue"
                else -> "Upcoming"
            }
            val givenStr = v.completedEpochDay?.let { LocalDate.ofEpochDay(it).format(dateFmt) } ?: "-"
            vaccineRows.add(
                listOf(
                    xCell(v.ageGroupLabel, style),
                    xCell(v.name, style),
                    xCell(dueStr, style),
                    xCell(statusStr, style),
                    xCell(givenStr, style),
                    xCell(v.protectionAgainst, style)
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

    private data class XCell(val valStr: String, val styleIndex: Int)

    private fun buildWorksheetXml(rows: List<List<XCell>>, colWidths: List<Int>): String {
        val colsXml = colWidths.mapIndexed { idx, w ->
            """<col min="${idx + 1}" max="${idx + 1}" width="$w" customWidth="1"/>"""
        }.joinToString("")

        val rowsXml = rows.mapIndexed { rIdx, row ->
            val cellsXml = row.mapIndexed { cIdx, cell ->
                val colLetter = ('A' + cIdx).toString()
                val ref = "$colLetter${rIdx + 1}"
                """<c r="$ref" s="${cell.styleIndex}" t="inlineStr"><is><t>${escapeXml(cell.valStr)}</t></is></c>"""
            }.joinToString("")
            """<row r="${rIdx + 1}">$cellsXml</row>"""
        }.joinToString("")

        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <cols>$colsXml</cols>
    <sheetData>$rowsXml</sheetData>
</worksheet>"""
    }

    private fun escapeXml(input: String): String = input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&apos;")

    private const val CONTENT_TYPES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
    <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
    <Default Extension="xml" ContentType="application/xml"/>
    <Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>
    <Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet2.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet3.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/worksheets/sheet4.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>
    <Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>
</Types>"""

    private const val ROOT_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>
</Relationships>"""

    private const val WORKBOOK_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
    <sheets>
        <sheet name="Baby Profile" sheetId="1" r:id="rId1"/>
        <sheet name="Activity Logs" sheetId="2" r:id="rId2"/>
        <sheet name="Growth Tracker" sheetId="3" r:id="rId3"/>
        <sheet name="Vaccine Schedule" sheetId="4" r:id="rId4"/>
    </sheets>
</workbook>"""

    private const val WORKBOOK_RELS_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
    <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>
    <Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet2.xml"/>
    <Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet3.xml"/>
    <Relationship Id="rId4" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet4.xml"/>
    <Relationship Id="rId5" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>
</Relationships>"""

    private const val STYLES_XML = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">
    <fonts count="3">
        <font><sz val="11"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>
        <font><b/><sz val="11"/><name val="Calibri"/></font>
    </fonts>
    <fills count="5">
        <fill><patternFill patternType="none"/></fill>
        <fill><patternFill patternType="gray125"/></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FF1F4E78"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFF2F2F2"/><bgColor indexed="64"/></patternFill></fill>
        <fill><patternFill patternType="solid"><fgColor rgb="FFFFFFFF"/><bgColor indexed="64"/></patternFill></fill>
    </fills>
    <borders count="1">
        <border><left/><right/><top/><bottom/><diagonal/></border>
    </borders>
    <cellXfs count="5">
        <xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>
        <xf numFmtId="0" fontId="1" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
        <xf numFmtId="0" fontId="2" fillId="2" borderId="0" xfId="0" applyFont="1" applyFill="1"/>
        <xf numFmtId="0" fontId="0" fillId="3" borderId="0" xfId="0" applyFill="1"/>
        <xf numFmtId="0" fontId="0" fillId="4" borderId="0" xfId="0" applyFill="1"/>
    </cellXfs>
</styleSheet>"""
}
