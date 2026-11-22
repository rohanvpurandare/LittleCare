package com.littlecare.babytracker.data

enum class ActivityType {
    FEED, POOP, PEE
}

enum class FeedSubType(val label: String) {
    BREAST("Breast"),
    BOTTLE("Bottle"),
    FORMULA("Formula")
}

enum class PoopConsistency(val label: String) {
    NORMAL("Normal"),
    LOOSE("Loose / Watery"),
    HARD("Hard / Pellets"),
    MUCOUSY("Mucousy")
}
