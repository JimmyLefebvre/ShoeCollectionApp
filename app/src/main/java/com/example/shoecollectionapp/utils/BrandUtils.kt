package com.example.shoecollectionapp.utils

object BrandUtils {
    fun normalizeBrand(brand: String): String {
        return brand.trim().lowercase().replaceFirstChar { it.uppercase() }
    }

    fun findSuggestion(typed: String, existingBrands: List<String>): String? {
        val cleanTyped = typed.trim().lowercase()
        if (cleanTyped.length < 2) return null

        for (existing in existingBrands) {
            val cleanExisting = existing.trim().lowercase()
            if (cleanTyped == cleanExisting) continue
            val dist = levenshteinDistance(cleanTyped, cleanExisting)
            if (dist <= 2 && dist < cleanTyped.length) {
                return existing
            }
        }
        return null
    }

    private fun levenshteinDistance(lhs: CharSequence, rhs: CharSequence): Int {
        val lhsLength = lhs.length
        val rhsLength = rhs.length
        var cost = IntArray(lhsLength + 1) { it }
        var newCost = IntArray(lhsLength + 1) { 0 }
        for (i in 1..rhsLength) {
            newCost[0] = i
            for (j in 1..lhsLength) {
                val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1
                newCost[j] = minOf(
                    cost[j] + 1,
                    newCost[j - 1] + 1,
                    cost[j - 1] + match
                )
            }
            val swap = cost
            cost = newCost
            newCost = swap
        }
        return cost[lhsLength]
    }
}
