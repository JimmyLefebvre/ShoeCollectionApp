package com.example.shoecollectionapp.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    private const val MAX_SIDE_PX = 1600
    private const val JPEG_QUALITY = 85
    private const val FILE_PREFIX = "shoe_"

    /**
     * Copie une image (Galerie ou Caméra) vers le stockage interne, réduite et recompressée.
     * À appeler hors du thread principal.
     *
     * @return le chemin absolu du fichier, ou null si rien n'a pu être écrit. En cas d'échec,
     * aucun fichier partiel ne reste sur le disque.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        val resolver = context.contentResolver
        val target = File(context.filesDir, "$FILE_PREFIX${UUID.randomUUID()}.jpg")
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            val opened = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, bounds)
                true
            } ?: false
            if (!opened || bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

            val options = BitmapFactory.Options().apply {
                inSampleSize = computeSampleSize(bounds.outWidth, bounds.outHeight)
            }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return null

            // Le ré-encodage perd l'orientation EXIF : on l'applique aux pixels.
            val rotation = resolver.openInputStream(uri)?.use { readExifRotation(it) } ?: 0
            val finalBitmap = if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                val rotated = Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, matrix, true)
                if (rotated !== decoded) decoded.recycle()
                rotated
            } else {
                decoded
            }

            val written = FileOutputStream(target).use { out ->
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            }
            finalBitmap.recycle()

            if (!written) {
                target.delete()
                return null
            }
            target.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            target.delete()
            null
        }
    }

    /**
     * Supprime une photo créée par saveImageToInternalStorage. Refuse tout fichier situé
     * hors du stockage interne de l'app ou qui ne porte pas le préfixe des photos de chaussures.
     */
    fun deleteInternalImage(context: Context, path: String): Boolean {
        if (path.isBlank()) return false
        val file = File(path)
        val inFilesDir = file.parentFile?.canonicalPath == context.filesDir.canonicalPath
        return inFilesDir && file.name.startsWith(FILE_PREFIX) && file.delete()
    }

    /** Crée un fichier temporaire pour recevoir la photo de l'appareil photo. */
    fun createTempImageFile(context: Context): File {
        val fileName = "temp_shoe_${System.currentTimeMillis()}.jpg"
        return File(context.cacheDir, fileName)
    }

    private fun computeSampleSize(width: Int, height: Int): Int {
        var sample = 1
        while (maxOf(width, height) / (sample * 2) >= MAX_SIDE_PX) sample *= 2
        return sample
    }

    private fun readExifRotation(input: InputStream): Int {
        return try {
            when (
                ExifInterface(input).getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            ) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } catch (e: Exception) {
            0
        }
    }
}
