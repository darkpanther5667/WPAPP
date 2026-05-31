package com.aistudio.sharmakhata.pqmzvk

import android.content.Context
import android.os.Build
import android.os.StrictMode
import com.aistudio.sharmakhata.pqmzvk.data.local.AppDatabase
import com.aistudio.sharmakhata.pqmzvk.receiver.ReminderScheduler
import com.aistudio.sharmakhata.pqmzvk.util.NotificationHelper
import com.aistudio.sharmakhata.pqmzvk.util.SessionManager
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter

object GrahbookApp {
    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        initialized = true

        val app = context.applicationContext

        // Enable StrictMode for development
        if (BuildConfig.DEBUG) {
            enableStrictMode()
        }

        // Set up global exception handler
        setupGlobalExceptionHandler()

        SessionManager.load(app)
        NotificationHelper.createChannels(app)
        ReminderScheduler.scheduleDailySummary(app)
    }

    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .build()
        )
    }

    private fun setupGlobalExceptionHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            android.util.Log.e("GrahbookApp", "Uncaught exception in thread ${thread.name}", throwable)

            try {
                val stackTrace = StringWriter().also {
                    PrintWriter(it).use { writer ->
                        throwable.printStackTrace(writer)
                    }
                }.toString()
                android.util.Log.e("GrahbookApp", "Uncaught: $stackTrace")
            } catch (_: Exception) { }

            defaultHandler?.uncaughtException(thread, throwable)
        }
    }
}
