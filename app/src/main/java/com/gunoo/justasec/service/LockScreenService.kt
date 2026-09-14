package com.gunoo.justasec.service

import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Settings
import com.gunoo.justasec.ui.LockScreenActivity
import com.gunoo.justasec.ui.MainActivity

/**
 * 화면이 켜질 때(ACTION_SCREEN_ON) 잠금 상태면 LockScreenActivity를 띄우는 상시 서비스.
 * ACTION_SCREEN_ON은 manifest 등록이 불가능해서 서비스에서 동적 등록해야 함.
 */
class LockScreenService : Service() {

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action != Intent.ACTION_SCREEN_ON) return

            val keyguard = context.getSystemService(KeyguardManager::class.java)
            val canOverlay = Settings.canDrawOverlays(context)

            // 잠금 상태 + 오버레이 권한이 있고, 전화가 오는 중이 아닐 때만 개입
            if (keyguard.isKeyguardLocked && canOverlay && !CallGuard.isCallActive(context)) {
                context.startActivity(
                    Intent(context, LockScreenActivity::class.java).addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    )
                )
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, buildNotification())
        registerReceiver(screenReceiver, IntentFilter(Intent.ACTION_SCREEN_ON))
        setRunning(this, true)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // 시스템이 죽여도 다시 살리도록
        return START_STICKY
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenReceiver) }
        setRunning(this, false)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "잠금화면 퀴즈",
                NotificationManager.IMPORTANCE_MIN, // 최대한 조용하게
            ).apply { setShowBadge(false) }
        )

        val tapIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )

        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_sort_alphabetically)
            .setContentTitle("영단어 퀴즈 실행 중")
            .setContentText("화면이 켜지면 잠금화면에 퀴즈가 표시됩니다")
            .setContentIntent(tapIntent)
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "lockscreen_quiz"
        private const val NOTIFICATION_ID = 1
        private const val PREFS_NAME = "vocab_service_state"
        private const val KEY_RUNNING = "running"

        fun start(context: Context) {
            context.startForegroundService(Intent(context, LockScreenService::class.java))
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LockScreenService::class.java))
            // stopService()는 비동기라 onDestroy를 바로 보장하지 않으므로 즉시 상태도 반영해둔다.
            setRunning(context, false)
        }

        /** MainActivity가 실제 서비스 상태를 읽기 위한 값. onCreate/onDestroy에서 기록됨. */
        fun isRunning(context: Context): Boolean =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_RUNNING, false)

        private fun setRunning(context: Context, running: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_RUNNING, running)
                .apply()
        }
    }
}
