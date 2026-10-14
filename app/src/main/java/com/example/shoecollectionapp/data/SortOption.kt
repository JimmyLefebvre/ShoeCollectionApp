package com.example.shoecollectionapp.data

enum class SortOption(val label: String) {
    NAME_ASC("Nom (A ↗ Z)"),
    NAME_DESC("Nom (Z ↘ A)"),
    BRAND_ASC("Marque (A ↗ Z)"),
    BRAND_DESC("Marque (Z ↘ A)"),
    DATE_DESC("Date (Plus récente)"),
    DATE_ASC("Date (Plus ancienne)")
}
