package com.example.myancast.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * ★ Phase 6 (#4) — v3 (Phase 5 build) ရှိတဲ့ ဖုန်းကို v4 update လုပ်ရင် subscribe/history **မပျောက်ရ**။
 * Migration SQL က Room မျှော်လင့်တာနဲ့ မကိုက်ရင် Room က open ချိန်မှာ exception ပစ်လို့ ဒီ test fail မယ်။
 */
@RunWith(RobolectricTestRunner::class)
class AppDatabaseMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val dbName = "migration-test.db"

    @Before
    fun createVersion3Database() {
        context.deleteDatabase(dbName)
        val file = context.getDatabasePath(dbName).apply { parentFile?.mkdirs() }
        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            // Phase 5 (v3) မှာ Room generate လုပ်ခဲ့တဲ့ table အတိုင်း
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `library` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, " +
                    "`podcastId` TEXT NOT NULL, `positionMs` INTEGER NOT NULL, `durationMs` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, `episodeTitle` TEXT NOT NULL, `podcastTitle` TEXT NOT NULL, " +
                    "`coverUrl` TEXT NOT NULL, PRIMARY KEY(`id`))"
            )
            db.execSQL(
                "INSERT INTO library VALUES ('p1', 'sub', 'p1', 0, 0, 100, '', '', '')"
            )
            db.execSQL(
                "INSERT INTO library VALUES ('e1', 'history', 'p1', 60000, 600000, 200, 'အပိုင်း ၁', 'နည်းပညာ', '')"
            )
            db.version = 3
        }
    }

    @After
    fun tearDown() {
        context.deleteDatabase(dbName)
    }

    @Test
    fun `migrating 3 to 4 keeps subscriptions and history and adds downloads`() = runTest {
        val db = Room.databaseBuilder(context, AppDatabase::class.java, dbName)
            .addMigrations(AppDatabase.MIGRATION_3_4)   // fallback မပါ — migration မှန်မှ ဖွင့်ရ
            .allowMainThreadQueries()
            .build()

        try {
            val subs = db.libraryDao().byType(LibraryEntity.TYPE_SUB).first()
            val history = db.libraryDao().byType(LibraryEntity.TYPE_HISTORY).first()
            assertEquals(listOf("p1"), subs.map { it.id })
            assertEquals("အပိုင်း ၁", history.single().episodeTitle)
            assertEquals(60_000L, history.single().positionMs)

            db.downloadDao().upsert(
                DownloadEntity(
                    episodeId = "e1", podcastId = "p1", podcastTitle = "နည်းပညာ", title = "အပိုင်း ၁",
                    description = "", audioUrl = "https://a.com/1.mp3", duration = 600, publishedAtMillis = null,
                    coverUrl = "", fileSizeBytes = 0L, status = DownloadEntity.STATUS_QUEUED, progress = 0,
                    wifiOnly = true, localPath = "", downloadedBytes = 0L, updatedAt = 1L
                )
            )
            assertNotNull(db.downloadDao().get("e1"))
        } finally {
            db.close()
        }
    }
}
