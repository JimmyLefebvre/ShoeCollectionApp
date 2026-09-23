package com.example.shoecollectionapp.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageUtils {

    /**
     * Copie une image depuis une Uri (Galerie ou Caméra) vers le stockage interne privé de l'app.
     * @return Le chemin absolu du fichier enregistré sous forme de String.
     */
    fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val fileName = "shoe_${UUID.randomUUID()}.jpg"
            val file = File(context.filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Crée un fichier temporaire pour recevoir la photo de l'appareil photo avant sauvegarde finale.
     */
    fun createTempImageFile(context: Context): File {
        val fileName = "temp_shoe_${System.currentTimeMillis()}.jpg"
        return File(context.cacheDir, fileName)
    }
}