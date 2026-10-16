package com.example.shoecollectionapp.utils

/**
 * Géométrie de la grille de rangement.
 *
 * Les chaussures sont stockées en index de case (gridCol, gridRow), indépendants de la densité
 * de l'écran. Les étiquettes, positionnées librement, sont stockées en dp.
 */
object GridUtils {
    const val COLS = 25
    const val ROWS = 25

    const val CELL_W_DP = 110f
    const val CELL_H_DP = 70f

    /**
     * Case libre la plus proche de (col, row), distance euclidienne.
     * Renvoie la case demandée si elle est libre, null si la grille est pleine.
     */
    fun nearestFreeCell(
        col: Int,
        row: Int,
        occupied: Set<Pair<Int, Int>>
    ): Pair<Int, Int>? {
        val wantedCol = col.coerceIn(0, COLS - 1)
        val wantedRow = row.coerceIn(0, ROWS - 1)
        if (Pair(wantedCol, wantedRow) !in occupied) return Pair(wantedCol, wantedRow)

        var best: Pair<Int, Int>? = null
        var bestDistance = Int.MAX_VALUE
        for (c in 0 until COLS) {
            for (r in 0 until ROWS) {
                if (Pair(c, r) in occupied) continue
                val dc = c - wantedCol
                val dr = r - wantedRow
                val distance = dc * dc + dr * dr
                if (distance < bestDistance) {
                    bestDistance = distance
                    best = Pair(c, r)
                }
            }
        }
        return best
    }
}
