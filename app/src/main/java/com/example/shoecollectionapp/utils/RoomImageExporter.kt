package com.example.shoecollectionapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
import androidx.core.graphics.toColorInt
import androidx.core.graphics.createBitmap

object RoomImageExporter {

    fun generateRoomBitmap(
        context: Context,
        piece: Piece,
        shoes: List<Chaussure>,
        etiquettes: List<Etiquette>
    ): Uri? {
        val pieceShoes = shoes.filter { it.idPiece == piece.id }
        val pieceEtiquettes = etiquettes.filter { it.idPiece == piece.id }

        // Rendu à l'échelle x2 par rapport aux dp de l'écran
        val scale = 2f
        val cellWidth = GridUtils.CELL_W_DP * scale
        val cellHeight = GridUtils.CELL_H_DP * scale

        // Boîte englobante du contenu, en pixels d'export
        var minLeft = Float.MAX_VALUE
        var minTop = Float.MAX_VALUE
        var maxRight = 0f
        var maxBottom = 0f
        pieceShoes.forEach {
            val left = it.gridCol * cellWidth
            val top = it.gridRow * cellHeight
            minLeft = minOf(minLeft, left)
            minTop = minOf(minTop, top)
            maxRight = maxOf(maxRight, left + cellWidth)
            maxBottom = maxOf(maxBottom, top + cellHeight)
        }
        pieceEtiquettes.forEach {
            val left = it.x * scale
            val top = it.y * scale
            minLeft = minOf(minLeft, left)
            minTop = minOf(minTop, top)
            maxRight = maxOf(maxRight, left + 400f)
            maxBottom = maxOf(maxBottom, top + 60f)
        }
        if (minLeft == Float.MAX_VALUE) {
            minLeft = 0f; minTop = 0f; maxRight = cellWidth * 4; maxBottom = cellHeight * 3
        }

        // Alignement sur la grille pour que les lignes tombent sur les cases
        val startX = kotlin.math.floor(minLeft / cellWidth) * cellWidth
        val startY = kotlin.math.floor(minTop / cellHeight) * cellHeight
        val bitmapWidth = (kotlin.math.ceil((maxRight - startX) / cellWidth) * cellWidth).toInt().coerceAtLeast(400)
        val bitmapHeight = (kotlin.math.ceil((maxBottom - startY) / cellHeight) * cellHeight).toInt().coerceAtLeast(300)

        val bitmap = createBitmap(bitmapWidth, bitmapHeight)
        val canvas = Canvas(bitmap)

        // Fond
        val bgPaint = Paint().apply { color = "#1C1B1F".toColorInt() }
        canvas.drawRect(0f, 0f, bitmapWidth.toFloat(), bitmapHeight.toFloat(), bgPaint)

        // Grille
        val gridPaint = Paint().apply {
            color = "#50FFFFFF".toColorInt()
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
            color = "#FFFBFE".toColorInt()
            style = Paint.Style.FILL
        }
        val cardBorderPaint = Paint().apply {
            color = "#79747E".toColorInt()
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val textPaint = Paint().apply {
            color = "#1C1B1F".toColorInt()
            textSize = 24f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val brandPaint = Paint().apply {
            color = "#6750A4".toColorInt()
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        pieceShoes.forEach { shoe ->
            val left = (shoe.gridCol * cellWidth) - startX
            val top = (shoe.gridRow * cellHeight) - startY
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
                    val sampling = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(photoFile.absolutePath, sampling)
                    var sample = 1
                    while (sampling.outWidth / (sample * 2) >= 208 && sampling.outHeight / (sample * 2) >= 208) sample *= 2
                    val photoBmp = BitmapFactory.decodeFile(
                        photoFile.absolutePath,
                        BitmapFactory.Options().apply { inSampleSize = sample }
                    )
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
            color = "#EADDFF".toColorInt()
            style = Paint.Style.FILL
        }
        val tagBorderPaint = Paint().apply {
            color = "#6750A4".toColorInt()
            strokeWidth = 3f
            style = Paint.Style.STROKE
        }
        val tagTextPaint = Paint().apply {
            color = "#21005D".toColorInt()
            textSize = 26f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        pieceEtiquettes.forEach { tag ->
            val left = (tag.x * scale) - startX
            val top = (tag.y * scale) - startY
            val textWidth = tagTextPaint.measureText(tag.texte)
            val rectWidth = textWidth + 50f
            val rectHeight = 60f

            val rect = RectF(left, top, left + rectWidth, top + rectHeight)
            canvas.drawRoundRect(rect, 30f, 30f, tagBgPaint)
            canvas.drawRoundRect(rect, 30f, 30f, tagBorderPaint)
            canvas.drawText(tag.texte, left + 20f, top + 40f, tagTextPaint)
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
