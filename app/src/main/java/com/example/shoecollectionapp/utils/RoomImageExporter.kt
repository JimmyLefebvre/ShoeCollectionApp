package com.example.shoecollectionapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.shoecollectionapp.data.Chaussure
import com.example.shoecollectionapp.data.Etiquette
import com.example.shoecollectionapp.data.Piece
import java.io.File
import java.io.FileOutputStream

object RoomImageExporter {

    fun generateRoomBitmap(
        context: Context,
        piece: Piece,
        shoes: List<Chaussure>,
        etiquettes: List<Etiquette>
    ): Uri? {
        val pieceShoes = shoes.filter { it.idPiece == piece.id }
        val pieceEtiquettes = etiquettes.filter { it.idPiece == piece.id }

        val cellWidth = 110f * 2f
        val cellHeight = 70f * 2f

        // Boîte englobante
        val minCol = minOf(
            pieceShoes.minOfOrNull { (it.posX / cellWidth).toInt() } ?: 0,
            pieceEtiquettes.minOfOrNull { (it.x / cellWidth).toInt() } ?: 0,
            0
        ).coerceAtLeast(0)

        val minRow = minOf(
            pieceShoes.minOfOrNull { (it.posY / cellHeight).toInt() } ?: 0,
            pieceEtiquettes.minOfOrNull { (it.y / cellHeight).toInt() } ?: 0,
            0
        ).coerceAtLeast(0)

        val maxCol = maxOf(
            pieceShoes.maxOfOrNull { ((it.posX + cellWidth) / cellWidth).toInt() } ?: 5,
            pieceEtiquettes.maxOfOrNull { ((it.x + 200f) / cellWidth).toInt() } ?: 5,
            5
        ).coerceAtMost(25)

        val maxRow = maxOf(
            pieceShoes.maxOfOrNull { ((it.posY + cellHeight) / cellHeight).toInt() } ?: 5,
            pieceEtiquettes.maxOfOrNull { ((it.y + 60f) / cellHeight).toInt() } ?: 5,
            5
        ).coerceAtMost(25)

        val startX = (minCol * cellWidth)
        val startY = (minRow * cellHeight)
        val bitmapWidth = ((maxCol - minCol + 1) * cellWidth).toInt().coerceAtLeast(400)
        val bitmapHeight = ((maxRow - minRow + 1) * cellHeight).toInt().coerceAtLeast(300)

        val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Fond
        val bgPaint = Paint().apply { color = Color.parseColor("#1C1B1F") }
        canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), bgPaint)

        // Grille
        val gridPaint = Paint().apply {
            color = Color.parseColor("#50FFFFFF")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }

        var x = 0f
        while (x <= bitmapWidth) {
            canvas.drawLine(x, 0f, x, bitmapHeight.toFloat(), gridPaint)
            x += cellWidth
        }
        var y = 0f
        while (y <= bitmapHeight) {
            canvas.drawLine(0f, y, bitmapWidth.toFloat(), y, gridPaint)
            y += cellHeight
        }

        // Peintures cartes chaussures
        val cardPaint = Paint().apply {
            color = Color.parseColor("#FFFBFE")
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = Color.parseColor("#79747E")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val textPaint = Paint().apply {
            color = Color.parseColor("#1C1B1F")
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val brandPaint = Paint().apply {
            color = Color.parseColor("#6750A4")
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        pieceShoes.forEach { shoe ->
            val left = (shoe.posX * 2f) - startX
            val top = (shoe.posY * 2f) - startY
            val right = left + cellWidth
            val bottom = top + cellHeight

            canvas.drawRect(left, top, right, bottom, cardPaint)
            canvas.drawRect(left, top, right, bottom, cardBorderPaint)

            // Texte nom et marque
            canvas.drawText(shoe.nom.take(14), left + 16f, top + 42f, textPaint)
            if (shoe.marque.isNotBlank()) {
                canvas.drawText(shoe.marque.take(16), left + 16f, top + 76f, brandPaint)
            }

            // Miniature photo sur le côté droit
            if (shoe.photoPath.isNotBlank()) {
                val photoFile = File(shoe.photoPath)
                if (photoFile.exists()) {
                    val photoBmp = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (photoBmp != null) {
                        val imgSize = 104f // 52dp * 2
                        val imgLeft = right - imgSize - 12f
                        val imgTop = top + (cellHeight - imgSize) / 2f
                        val srcRect = Rect(0, 0, photoBmp.width, photoBmp.height)
                        val destRect = RectF(imgLeft, imgTop, imgLeft + imgSize, imgTop + imgSize)
                        canvas.drawBitmap(photoBmp, srcRect, destRect, null)
                        photoBmp.recycle()
                    }
                }
            }
        }

        // Étiquettes au premier plan
        val tagBgPaint = Paint().apply {
            color = Color.parseColor("#EADDFF")
            style = Paint.Style.FILL
        }
        val tagBorderPaint = Paint().apply {
            color = Color.parseColor("#6750A4")
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val tagTextPaint = Paint().apply {
            color = Color.parseColor("#21005D")
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        pieceEtiquettes.forEach { tag ->
            val left = (tag.x * 2f) - startX
            val top = (tag.y * 2f) - startY
            val textWidth = tagTextPaint.measureText(tag.texte)
            val rectWidth = textWidth + 50f
            val rectHeight = 60f

            val rect = RectF(left, top, left + rectWidth, top + rectHeight)
            canvas.drawRoundRect(rect, 30f, 30f, tagBgPaint)
            canvas.drawRoundRect(rect, 30f, 30f, tagBorderPaint)
            canvas.drawText("🏷️ " + tag.texte, left + 20f, top + 40f, tagTextPaint)
        }

        val imagesDir = File(context.cacheDir, "exported_rooms")
        imagesDir.mkdirs()
        val imageFile = File(imagesDir, "Vue_${piece.nom.replace(" ", "_")}.png")
        FileOutputStream(imageFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
    }
}
