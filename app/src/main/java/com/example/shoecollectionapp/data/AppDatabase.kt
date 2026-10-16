package com.example.shoecollectionapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Piece::class, Section::class, Chaussure::class, Etiquette::class], version = 7, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shoe_collection_db"
                )
                    .addMigrations(Migration6To7(context))
                    // Anciennes versions de dev sans chemin de migration : seules celles-ci
                    // peuvent encore perdre leurs données.
                    .fallbackToDestructiveMigrationFrom(true, 1, 2, 3, 4, 5)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
