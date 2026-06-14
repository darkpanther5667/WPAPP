package com.aistudio.sharmakhata.pqmzvk.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CacheEntry::class, PendingOperation::class, ItemEntity::class, ExpenseEntity::class, PurchaseEntity::class],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cacheDao(): CacheDao
    abstract fun pendingDao(): PendingDao
    abstract fun itemDao(): ItemDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun purchaseDao(): PurchaseDao

    /**
     * Clear all cached API data and pending ops.
     * Call this when the user logs out or switches stores.
     */
    suspend fun clearCache() {
        cacheDao().clearAll()
        pendingDao().clearAll()
    }

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Toggle this to `true` only during local development.
         *
         * In production builds, [ALLOW_DESTRUCTIVE_MIGRATION] must remain `false`
         * so that schema changes always go through a proper [Migration], preserving
         * user data.  When `false` and a migration path is missing, Room will crash
         * at startup with an [IllegalStateException] — this is intentional so the
         * developer is forced to write a migration rather than silently nuking data.
         */
        private const val ALLOW_DESTRUCTIVE_MIGRATION = false

        // ──────────────────────────────────────────────────────────────
        //  Migration 1 → 2
        //
        //  What changed:
        //    • Added `expenses`  table  — record daily business expenses
        //    • Added `purchases` table — track supplier purchases
        // ──────────────────────────────────────────────────────────────
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                android.util.Log.i("AppDatabase", "MIGRATION_1_2: Creating expenses and purchases tables")

                try {
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `expenses` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `amount` REAL NOT NULL,
                            `category` TEXT NOT NULL DEFAULT 'Other',
                            `note` TEXT,
                            `createdAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `purchases` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `supplierName` TEXT NOT NULL,
                            `supplierPhone` TEXT NOT NULL DEFAULT '',
                            `itemsJson` TEXT NOT NULL DEFAULT '[]',
                            `totalAmount` REAL NOT NULL,
                            `paidAmount` REAL NOT NULL DEFAULT 0.0,
                            `status` TEXT NOT NULL DEFAULT 'unpaid',
                            `notes` TEXT NOT NULL DEFAULT '',
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    android.util.Log.i("AppDatabase", "MIGRATION_1_2 completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_1_2 failed", e)
                    throw e
                }
            }
        }

        // ──────────────────────────────────────────────────────────────
        //  Migration 2 → 3
        //
        //  What changed:
        //    • `items`     — added hsnCode, lastPrice, stock, lowStockAlert,
        //                   updatedAt (stock-management feature)
        //    • `purchases` — added supplierPhone, itemsJson, paidAmount,
        //                   notes, updatedAt (richer purchase records)
        //
        //  Defensive strategy:
        //    1. Re-create every table with `CREATE TABLE IF NOT EXISTS` so
        //       tables that were missing from previous migrations are still
        //       created.
        //    2. Attempt each `ALTER TABLE … ADD COLUMN` inside a
        //       try‑catch — the ALTER silently becomes a no‑op when the
        //       column already exists.
        // ──────────────────────────────────────────────────────────────
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                android.util.Log.i("AppDatabase", "MIGRATION_2_3: Adding stock-management columns")

                try {
                    // ── Step 1 — ensure every table exists ────────────
                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `items` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `name` TEXT NOT NULL,
                            `price` REAL NOT NULL,
                            `hsnCode` TEXT NOT NULL DEFAULT '',
                            `lastPrice` REAL NOT NULL DEFAULT 0.0,
                            `stock` INTEGER NOT NULL DEFAULT 0,
                            `lowStockAlert` INTEGER NOT NULL DEFAULT 5,
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `expenses` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `title` TEXT NOT NULL,
                            `amount` REAL NOT NULL,
                            `category` TEXT NOT NULL DEFAULT 'Other',
                            `note` TEXT,
                            `createdAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `purchases` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `supplierName` TEXT NOT NULL,
                            `supplierPhone` TEXT NOT NULL DEFAULT '',
                            `itemsJson` TEXT NOT NULL DEFAULT '[]',
                            `totalAmount` REAL NOT NULL,
                            `paidAmount` REAL NOT NULL DEFAULT 0.0,
                            `status` TEXT NOT NULL DEFAULT 'unpaid',
                            `notes` TEXT NOT NULL DEFAULT '',
                            `createdAt` INTEGER NOT NULL,
                            `updatedAt` INTEGER NOT NULL
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `cache_entries` (
                            `key` TEXT NOT NULL,
                            `json` TEXT NOT NULL,
                            `updatedAt` INTEGER NOT NULL,
                            PRIMARY KEY(`key`)
                        )
                    """.trimIndent())

                    db.execSQL("""
                        CREATE TABLE IF NOT EXISTS `pending_operations` (
                            `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                            `type` TEXT NOT NULL,
                            `payload` TEXT NOT NULL,
                            `createdAt` INTEGER NOT NULL,
                            `retries` INTEGER NOT NULL DEFAULT 0
                        )
                    """.trimIndent())

                    // ── Step 2 — add columns that might be missing ────
                    // items – stock management fields
                    tryAlter(db, "items", "hsnCode",        "TEXT NOT NULL DEFAULT ''")
                    tryAlter(db, "items", "lastPrice",      "REAL NOT NULL DEFAULT 0.0")
                    tryAlter(db, "items", "stock",          "INTEGER NOT NULL DEFAULT 0")
                    tryAlter(db, "items", "lowStockAlert",  "INTEGER NOT NULL DEFAULT 5")
                    tryAlter(db, "items", "updatedAt",      "INTEGER NOT NULL DEFAULT 0")

                    // purchases – enriched fields
                    tryAlter(db, "purchases", "supplierPhone", "TEXT NOT NULL DEFAULT ''")
                    tryAlter(db, "purchases", "itemsJson",     "TEXT NOT NULL DEFAULT '[]'")
                    tryAlter(db, "purchases", "paidAmount",    "REAL NOT NULL DEFAULT 0.0")
                    tryAlter(db, "purchases", "notes",         "TEXT NOT NULL DEFAULT ''")
                    tryAlter(db, "purchases", "updatedAt",     "INTEGER NOT NULL DEFAULT 0")

                    android.util.Log.i("AppDatabase", "MIGRATION_2_3 completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_2_3 failed", e)
                    throw e
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                android.util.Log.i("AppDatabase", "MIGRATION_3_4: Adding serverId columns")
                try {
                    tryAlter(db, "purchases", "serverId", "TEXT NOT NULL DEFAULT ''")
                    tryAlter(db, "expenses", "serverId", "TEXT NOT NULL DEFAULT ''")
                    android.util.Log.i("AppDatabase", "MIGRATION_3_4 completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "MIGRATION_3_4 failed", e)
                    throw e
                }
            }
        }

        // ──────────────────────────────────────────────────────────────
        //  Helper — attempt ALTER TABLE ADD COLUMN, swallow if column
        //            already exists (SQLite error code 1).
        // ──────────────────────────────────────────────────────────────
        private fun tryAlter(db: SupportSQLiteDatabase, table: String, column: String, definition: String) {
            try {
                db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $definition")
                android.util.Log.d("AppDatabase", "Added column $table.$column")
            } catch (e: Exception) {
                // Column either already exists or the table does not exist yet
                android.util.Log.w("AppDatabase", "Skipped column $table.$column — ${e.message}")
            }
        }

        // ──────────────────────────────────────────────────────────────
        //  Database singleton
        // ──────────────────────────────────────────────────────────────
        fun get(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: try {
                    Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "grahbook.db"
                    )
                        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                        .apply {
                            if (ALLOW_DESTRUCTIVE_MIGRATION) {
                                fallbackToDestructiveMigration()
                            }
                        }
                        .build()
                        .also { INSTANCE = it }
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Failed to initialize database", e)
                    // Fallback: try with a new database name
                    try {
                        Room.databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "grahbook_fallback.db"
                        )
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                            .apply {
                                if (ALLOW_DESTRUCTIVE_MIGRATION) {
                                    fallbackToDestructiveMigration()
                                }
                            }
                            .build()
                            .also { INSTANCE = it }
                    } catch (fallbackException: Exception) {
                        android.util.Log.e("AppDatabase", "Fallback database initialization also failed", fallbackException)
                        throw RuntimeException("Failed to initialize database", fallbackException)
                    }
                }
            }
        }
    }
}
