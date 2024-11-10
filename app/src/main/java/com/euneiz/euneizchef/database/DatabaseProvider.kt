package com.euneiz.euneizchef.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    @Volatile
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "recipe_database"
            ).build().also { db = it }
        }
    }
}

