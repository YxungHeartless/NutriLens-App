package com.example.domain.model

enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack");

    companion object {
        fun fromString(value: String): MealType {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: BREAKFAST
        }
    }
}
