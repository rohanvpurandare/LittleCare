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
        VaccineTemplate("OPV_0", "OPV-0", "At Birth", 0, "Oral Polio Vaccine given within 15 days of birth"),
        VaccineTemplate("HEP_B_0", "Hepatitis B (Birth Dose)", "At Birth", 0, "Given within 24 hours of birth"),

        VaccineTemplate("OPV_1", "OPV-1", "6 Weeks", 42, "Oral Polio Vaccine - 1st dose"),
        VaccineTemplate("PENTA_1", "Pentavalent-1", "6 Weeks", 42, "Diphtheria, Pertussis, Tetanus, Hep B & Hib"),
        VaccineTemplate("ROTA_1", "Rotavirus-1 (RVW)", "6 Weeks", 42, "Protects against severe rotavirus diarrhoea"),
        VaccineTemplate("FIPV_1", "FIPV-1", "6 Weeks", 42, "Fractional Inactivated Polio Vaccine (intradermal)"),
        VaccineTemplate("PCV_1", "PCV-1", "6 Weeks", 42, "Pneumococcal Conjugate Vaccine - 1st dose"),

        VaccineTemplate("OPV_2", "OPV-2", "10 Weeks", 70, "Oral Polio Vaccine - 2nd dose"),
        VaccineTemplate("PENTA_2", "Pentavalent-2", "10 Weeks", 70, "DPT, Hep B & Hib - 2nd dose"),
        VaccineTemplate("ROTA_2", "Rotavirus-2 (RVW)", "10 Weeks", 70, "Rotavirus oral drops - 2nd dose"),

        VaccineTemplate("OPV_3", "OPV-3", "14 Weeks", 98, "Oral Polio Vaccine - 3rd dose"),
        VaccineTemplate("PENTA_3", "Pentavalent-3", "14 Weeks", 98, "DPT, Hep B & Hib - 3rd dose"),
        VaccineTemplate("ROTA_3", "Rotavirus-3 (RVW)", "14 Weeks", 98, "Rotavirus oral drops - 3rd dose"),
        VaccineTemplate("FIPV_2", "FIPV-2", "14 Weeks", 98, "Fractional Inactivated Polio Vaccine - 2nd dose"),
        VaccineTemplate("PCV_2", "PCV-2", "14 Weeks", 98, "Pneumococcal Conjugate Vaccine - 2nd dose"),

        VaccineTemplate("MR_1", "MR-1st Dose", "9 Months", 270, "Measles & Rubella - 1st dose"),
        VaccineTemplate("JE_1", "JE-1st Dose", "9 Months", 270, "Japanese Encephalitis (Endemic districts)"),
        VaccineTemplate("PCV_BOOSTER", "PCV Booster", "9 Months", 270, "Pneumococcal Conjugate Vaccine Booster"),
        VaccineTemplate("VIT_A_1", "Vitamin A (1st Dose)", "9 Months", 270, "1 ml (1 lakh IU) orally with MR-1"),

        VaccineTemplate("MR_2", "MR-2nd Dose", "16-24 Months", 480, "Measles & Rubella - 2nd dose"),
        VaccineTemplate("JE_2", "JE-2nd Dose", "16-24 Months", 480, "Japanese Encephalitis - 2nd dose"),
        VaccineTemplate("DPT_BOOSTER_1", "DPT Booster-1", "16-24 Months", 480, "Diphtheria, Pertussis & Tetanus Booster"),
        VaccineTemplate("OPV_BOOSTER", "OPV Booster", "16-24 Months", 480, "Oral Polio Vaccine Booster"),

        VaccineTemplate("DPT_BOOSTER_2", "DPT Booster-2", "5-6 Years", 1825, "DPT Booster - 2nd dose"),
        VaccineTemplate("TT_10", "Tetanus & adult Diphtheria (Td)", "10 Years", 3650, "Td booster at 10 years"),
        VaccineTemplate("TT_16", "Tetanus & adult Diphtheria (Td)", "16 Years", 5840, "Td booster at 16 years")
    )

    fun buildScheduleForBirthDate(dob: LocalDate): List<VaccineRecord> {
        return uipSchedule.map { item ->
            VaccineRecord(
                babyId = 0,
                code = item.code,
                name = item.name,
                ageGroupLabel = item.stageLabel,
                dueOffsetDays = item.daysAfterBirth,
                protectionAgainst = item.description,
                isCompleted = false
            )
        }
    }
}
