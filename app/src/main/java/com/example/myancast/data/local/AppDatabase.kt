package com.example.myancast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [LibraryEntity::class],
    // v2 → v3 — column `coverURL` → `coverUrl` (Phase 5)။ နာမည် ပြောင်းရင်လည်း schema hash
    // ပြောင်းလို့ version မတိုးရင် Room က "cannot verify the data integrity" နဲ့ crash တယ်။
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myancast.db"
                )
                    .fallbackToDestructiveMigration()   // ★ course project အတွက်
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}