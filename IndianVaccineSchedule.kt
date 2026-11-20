package com.littlecare.babytracker.domain

import com.littlecare.babytracker.data.MilestoneRecord
import com.littlecare.babytracker.data.VaccineRecord

object IndianVaccineSchedule {

    /**
     * Generates the Indian National Immunization Schedule (MoHFW / IAP)
     * dynamically anchored to the baby's birth date.
     */
    fun generateForBaby(babyId: Long): List<VaccineRecord> = listOf(
        // At Birth
        VaccineRecord(babyId = babyId, code = "BCG", name = "BCG", ageGroupLabel = "At Birth", dueOffsetDays = 0, protectionAgainst = "Tuberculosis"),
        VaccineRecord(babyId = babyId, code = "OPV-0", name = "OPV (Zero Dose)", ageGroupLabel = "At Birth", dueOffsetDays = 0, protectionAgainst = "Poliomyelitis"),
        VaccineRecord(babyId = babyId, code = "HEPB-0", name = "Hepatitis B (Birth Dose)", ageGroupLabel = "At Birth", dueOffsetDays = 0, protectionAgainst = "Hepatitis B"),

        // 6 Weeks (42 days)
        VaccineRecord(babyId = babyId, code = "PENTA-1", name = "Pentavalent-1 (DTwP/DTaP + Hib + HepB)", ageGroupLabel = "6 Weeks", dueOffsetDays = 42, protectionAgainst = "Diphtheria, Pertussis, Tetanus, Hib, Hep B"),
        VaccineRecord(babyId = babyId, code = "OPV-1", name = "OPV-1 / IPV-1", ageGroupLabel = "6 Weeks", dueOffsetDays = 42, protectionAgainst = "Poliomyelitis"),
        VaccineRecord(babyId = babyId, code = "ROTA-1", name = "Rotavirus-1", ageGroupLabel = "6 Weeks", dueOffsetDays = 42, protectionAgainst = "Rotavirus Diarrhoea"),
        VaccineRecord(babyId = babyId, code = "PCV-1", name = "PCV-1 (Pneumococcal)", ageGroupLabel = "6 Weeks", dueOffsetDays = 42, protectionAgainst = "Pneumococcal Pneumonia & Meningitis"),

        // 10 Weeks (70 days)
        VaccineRecord(babyId = babyId, code = "PENTA-2", name = "Pentavalent-2", ageGroupLabel = "10 Weeks", dueOffsetDays = 70, protectionAgainst = "Diphtheria, Pertussis, Tetanus, Hib, Hep B"),
        VaccineRecord(babyId = babyId, code = "OPV-2", name = "OPV-2", ageGroupLabel = "10 Weeks", dueOffsetDays = 70, protectionAgainst = "Poliomyelitis"),
        VaccineRecord(babyId = babyId, code = "ROTA-2", name = "Rotavirus-2", ageGroupLabel = "10 Weeks", dueOffsetDays = 70, protectionAgainst = "Rotavirus Diarrhoea"),

        // 14 Weeks (98 days)
        VaccineRecord(babyId = babyId, code = "PENTA-3", name = "Pentavalent-3", ageGroupLabel = "14 Weeks", dueOffsetDays = 98, protectionAgainst = "Diphtheria, Pertussis, Tetanus, Hib, Hep B"),
        VaccineRecord(babyId = babyId, code = "OPV-3", name = "OPV-3 / fIPV-2", ageGroupLabel = "14 Weeks", dueOffsetDays = 98, protectionAgainst = "Poliomyelitis"),
        VaccineRecord(babyId = babyId, code = "ROTA-3", name = "Rotavirus-3", ageGroupLabel = "14 Weeks", dueOffsetDays = 98, protectionAgainst = "Rotavirus Diarrhoea"),
        VaccineRecord(babyId = babyId, code = "PCV-2", name = "PCV-2 (Pneumococcal)", ageGroupLabel = "14 Weeks", dueOffsetDays = 98, protectionAgainst = "Pneumococcal Pneumonia & Meningitis"),

        // 6 Months (180 days)
        VaccineRecord(babyId = babyId, code = "FLU-1", name = "Influenza-1 (IAP Recommended)", ageGroupLabel = "6 Months", dueOffsetDays = 180, protectionAgainst = "Seasonal Influenza"),

        // 9 Months (270 days)
        VaccineRecord(babyId = babyId, code = "MR-1", name = "MR-1 (Measles & Rubella)", ageGroupLabel = "9 Months", dueOffsetDays = 270, protectionAgainst = "Measles & Rubella"),
        VaccineRecord(babyId = babyId, code = "PCV-B", name = "PCV Booster", ageGroupLabel = "9 Months", dueOffsetDays = 270, protectionAgainst = "Pneumococcal Booster"),
        VaccineRecord(babyId = babyId, code = "JE-1", name = "JE-1 (Endemic Districts)", ageGroupLabel = "9 Months", dueOffsetDays = 270, protectionAgainst = "Japanese Encephalitis"),
        VaccineRecord(babyId = babyId, code = "VITA-1", name = "Vitamin A (1st Dose - 1 Lakh IU)", ageGroupLabel = "9 Months", dueOffsetDays = 270, protectionAgainst = "Vitamin A Deficiency"),

        // 12 Months (365 days)
        VaccineRecord(babyId = babyId, code = "HEPA-1", name = "Hepatitis A-1", ageGroupLabel = "12 Months", dueOffsetDays = 365, protectionAgainst = "Hepatitis A"),

        // 16-18 Months (490 days)
        VaccineRecord(babyId = babyId, code = "DPT-B1", name = "DPT Booster-1 + Hib Booster", ageGroupLabel = "16–18 Months", dueOffsetDays = 490, protectionAgainst = "Diphtheria, Pertussis, Tetanus, Hib"),
        VaccineRecord(babyId = babyId, code = "MR-2", name = "MR-2 (Measles & Rubella)", ageGroupLabel = "16–18 Months", dueOffsetDays = 490, protectionAgainst = "Measles & Rubella"),
        VaccineRecord(babyId = babyId, code = "OPV-B", name = "OPV Booster", ageGroupLabel = "16–18 Months", dueOffsetDays = 490, protectionAgainst = "Poliomyelitis")
    )

    fun defaultMilestonesForBaby(babyId: Long): List<MilestoneRecord> = listOf(
        MilestoneRecord(babyId = babyId, monthRange = "Month 1–2", title = "First social smile & eye tracking"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 2–3", title = "Lifts head 45° during tummy time"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 3–4", title = "Coos, chuckles & brings hands to mouth"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 4–5", title = "Rolls from tummy to back"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 6", title = "Sits with minimal support & starts solids"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 7–8", title = "First tooth eruption & transfers toys hand-to-hand"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 9", title = "Crawls & responds to own name"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 10–11", title = "Pulls to stand & waves bye-bye"),
        MilestoneRecord(babyId = babyId, monthRange = "Month 12", title = "First independent steps & first meaningful word")
    )
}
