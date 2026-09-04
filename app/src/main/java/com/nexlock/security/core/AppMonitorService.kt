package com.nexlock.security.core

import android.app.*
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import com.nexlock.security.LockActivity
import com.nexlock.security.MainActivity

class AppMonitorService : Service() {
    private lateinit var usage: UsageStatsManager
    private lateinit var store: SecurityStore
    private var lastPackage: String? = null
    private var unlockedUntil = mutableMapOf<String, Long>()

    override fun onCreate() {
        super.onCreate()
        usage = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        store = SecurityStore(this)
        createChannel()
        startForeground(10, notification())
        Thread { loop() }.start()
    }

    private fun loop() {
        while (true) {
            val pkg = foregroundPackage()
            if (pkg != null && pkg != packageName && pkg != lastPackage && store.protectedPackages().contains(pkg)) {
                val until = unlockedUntil[pkg] ?: 0L
                if (System.currentTimeMillis() > until) {
                    val i = Intent(this, LockActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                        .putExtra("target_package", pkg)
                    startActivity(i)
                }
            }
            lastPackage = pkg
            Thread.sleep(450)
        }
    }

    private fun foregroundPackage(): String? {
        val end = System.currentTimeMillis()
        val events = usage.queryEvents(end - 3000, end)
        val event = UsageEvents.Event()
        var latest: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) latest = event.packageName
        }
        return latest
    }

    private fun notification(): Notification {
        val intent = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        return Notification.Builder(this, "nexlock_monitor")
            .setContentTitle("NexLock ativo")
            .setContentText("Proteção de aplicativos em execução")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setContentIntent(intent)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(NotificationChannel("nexlock_monitor", "Proteção NexLock", NotificationManager.IMPORTANCE_LOW))
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
