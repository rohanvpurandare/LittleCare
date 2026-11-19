package com.littlecare.babytracker.domain

import com.littlecare.babytracker.data.VaccineRecord
import java.time.LocalDate

object IndianVaccineSchedule {

    private data class VaccineTemplate(
        val code: String,
        val name: String,
        val stageLabel: String,
        val daysAfterBirth: Int,
        val description: String
    )

    private val uipSchedule = listOf(
        VaccineTemplate("BCG", "BCG", "At Birth", 0, "Protects against severe childhood tuberculosis"),
        VaccineTemplate("OPV_0", "OPV-0 (Zero Dose)", "At Birth", 0, "Oral Polio Vaccine given within 15 days of birth"),
        VaccineTemplate("HEP_B_0", "Hepatitis B (Birth Dose)", "At Birth", 0, "Given within 24 hours of birth"),

        VaccineTemplate("OPV_1", "OPV-1", "6 Weeks", 42, "Oral Polio Vaccine - 1st dose"),
        VaccineTemplate("PENTA_1", "Pentavalent-1", "6 Weeks", 42, "Diphtheria, Pertussis, Tetanus, Hep B & Hib"),
        VaccineTemplate("ROTA_1", "Rotavirus-1 (RVV)", "6 Weeks", 42, "Protects against severe rotavirus diarrhoea"),
        VaccineTemplate("FIPV_1", "fIPV-1", "6 Weeks", 42, "Fractional Inactivated Polio Vaccine (intradermal)"),
        VaccineTemplate("PCV_1", "PCV-1", "6 Weeks", 42, "Pneumococcal Conjugate Vaccine - 1st dose"),

        VaccineTemplate("OPV_2", "OPV-2", "10 Weeks", 70, "Oral Polio Vaccine - 2nd dose"),
        VaccineTemplate("PENTA_2", "Pentavalent-2", "10 Weeks", 70, "DPT, Hep B & Hib - 2nd dose"),
        VaccineTemplate("ROTA_2", "Rotavirus-2 (RVV)", "10 Weeks", 70, "Rotavirus oral drops - 2nd dose"),

        VaccineTemplate("OPV_3", "OPV-3", "14 Weeks", 98, "Oral Polio Vaccine - 3rd dose"),
        VaccineTemplate("PENTA_3", "Pentavalent-3", "14 Weeks", 98, "DPT, Hep B & Hib - 3rd dose"),
        VaccineTemplate("ROTA_3", "Rotavirus-3 (RVV)", "14 Weeks", 98, "Rotavirus oral drops - 3rd dose"),
        VaccineTemplate("FIPV_2", "fIPV-2", "14 Weeks", 98, "Fractional Inactivated Polio Vaccine - 2nd dose"),
        VaccineTemplate("PCV_2", "PCV-2", "14 Weeks", 98, "Pneumococcal Conjugate Vaccine - 2nd dose"),

        VaccineTemplate("MR_1", "MR-1 / MMR-1", "9–12 Months", 270, "Measles & Rubella - 1st dose"),
        VaccineTemplate("PCV_BOOSTER", "PCV-Booster", "9–12 Months", 270, "Pneumococcal Conjugate Booster dose"),
        VaccineTemplate("JE_1", "JE-1 (Endemic Districts)", "9–12 Months", 270, "Japanese Encephalitis vaccine (where applicable)"),
        VaccineTemplate("VIT_A_1", "Vitamin A (1st Dose)", "9–12 Months", 270, "1 lakh IU oral dose given with MR-1")
    )

    fun buildScheduleForBirthDate(birthDateEpochDay: Long): List<VaccineRecord> {
        val dob = LocalDate.ofEpochDay(birthDateEpochDay)
        return uipSchedule.map { item ->
            val dueDate = dob.plusDays(item.daysAfterBirth.toLong())
            VaccineRecord(
                code = item.code,
                name = item.name,
                stageLabel = item.stageLabel,
                daysAfterBirth = item.daysAfterBirth,
                dueDateEpochDay = dueDate.toEpochDay(),
                description = item.description
            )
        }
    }
}
