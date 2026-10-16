package com.example.shoecollectionapp.data

import android.content.Context
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shoecollectionapp.utils.GridUtils

/**
 * v6 -> v7
 * - chaussures : posX/posY (pixels) remplacés par gridCol/gridRow (index de case),
 *   ajout de l'index sur idPiece.
 * - etiquettes : x/y passent de pixels à dp.
 * - sections : ajout de l'index sur idPiece.
 *
 * La conversion suppose que les pixels stockés ont été produits avec la densité actuelle de
 * l'appareil, ce qui est vrai pour une base qui n'a pas été restaurée depuis un autre téléphone.
 */
class Migration6To7(context: Context) : Migration(6, 7) {

    private val density = context.applicationContext.resources.displayMetrics.density

    override fun migrate(db: SupportSQLiteDatabase) {
        val cellWidthPx = (GridUtils.CELL_W_DP * density).toDouble()
        val cellHeightPx = (GridUtils.CELL_H_DP * density).toDouble()
        val maxCol = GridUtils.COLS - 1
        val maxRow = GridUtils.ROWS - 1

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS chaussures_new (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                nom TEXT NOT NULL,
                marque TEXT NOT NULL,
                description TEXT NOT NULL,
                photoPath TEXT NOT NULL,
                idPiece INTEGER NOT NULL,
                idSection INTEGER,
                gridCol INTEGER NOT NULL,
                gridRow INTEGER NOT NULL,
                isFavorite INTEGER NOT NULL,
                createdDate INTEGER NOT NULL,
                positionIndex INTEGER NOT NULL,
                FOREIGN KEY(idPiece) REFERENCES pieces(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            INSERT INTO chaussures_new
                (id, nom, marque, description, photoPath, idPiece, idSection,
                 gridCol, gridRow, isFavorite, createdDate, positionIndex)
            SELECT
                id, nom, marque, description, photoPath, idPiece, idSection,
                MAX(0, MIN($maxCol, CAST(ROUND(posX / ?) AS INTEGER))),
                MAX(0, MIN($maxRow, CAST(ROUND(posY / ?) AS INTEGER))),
                isFavorite, createdDate, positionIndex
            FROM chaussures
            """.trimIndent(),
            arrayOf<Any>(cellWidthPx, cellHeightPx)
        )

        db.execSQL("DROP TABLE chaussures")
        db.execSQL("ALTER TABLE chaussures_new RENAME TO chaussures")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_chaussures_idPiece ON chaussures (idPiece)")

        db.execSQL(
            "UPDATE etiquettes SET x = x / ?, y = y / ?",
            arrayOf<Any>(density.toDouble(), density.toDouble())
        )

        db.execSQL("CREATE INDEX IF NOT EXISTS index_sections_idPiece ON sections (idPiece)")
    }
}
