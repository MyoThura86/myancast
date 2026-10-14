package com.example.myancast.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [LibraryEntity::class, DownloadEntity::class],
    // v2 → v3 — column `coverURL` → `coverUrl` (Phase 5)။ နာမည် ပြောင်းရင်လည်း schema hash
    // ပြောင်းလို့ version မတိုးရင် Room က "cannot verify the data integrity" နဲ့ crash တယ်။
    // v3 → v4 — ★ Phase 6 (#4) `downloads` table (MIGRATION_3_4 — subscribe/history မပျက်)
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun libraryDao(): LibraryDao
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * `downloads` table ထပ်ထည့်ရုံ — `library` (subscribe + history) ကို မထိ။
         * SQL က Room generate လုပ်တဲ့ `AppDatabase_Impl.createAllTables` နဲ့ စာလုံးတိုင်း တူရမယ်
         * (မတူရင် "Migration didn't properly handle" crash — `AppDatabaseMigrationTest` က စစ်)။
         */
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `downloads` (`episodeId` TEXT NOT NULL, " +
                        "`podcastId` TEXT NOT NULL, `podcastTitle` TEXT NOT NULL, `title` TEXT NOT NULL, " +
                        "`description` TEXT NOT NULL, `audioUrl` TEXT NOT NULL, `duration` INTEGER NOT NULL, " +
                        "`publishedAtMillis` INTEGER, `coverUrl` TEXT NOT NULL, `fileSizeBytes` INTEGER NOT NULL, " +
                        "`status` TEXT NOT NULL, `progress` INTEGER NOT NULL, `wifiOnly` INTEGER NOT NULL, " +
                        "`localPath` TEXT NOT NULL, `downloadedBytes` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`episodeId`))"
                )
            }
        }

        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "myancast.db"
                )
                    .addMigrations(MIGRATION_3_4)
                    // v2 လို အရမ်းဟောင်းတဲ့ DB အတွက် safety net (migration မရှိရင်ပဲ သုံး)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
