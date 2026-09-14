package com.gunoo.justasec.data

import android.content.Context

/**
 * 설정 화면(MainActivity)에서 저장하고 LockScreenActivity가 읽는 모드 설정.
 * - 순한맛(기본값): 언제든 닫기 가능
 * - 강제 모드: strictTarget개 문제를 풀어야 닫기 버튼이 나타남
 */
object QuizSettings {
    private const val PREFS_NAME = "vocab_settings"
    private const val KEY_STRICT_MODE = "strict_mode"
    private const val KEY_STRICT_TARGET = "strict_target"

    private const val DEFAULT_TARGET = 3
    val TARGET_RANGE = 1..10

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isStrictMode(context: Context): Boolean =
        prefs(context).getBoolean(KEY_STRICT_MODE, false)

    fun setStrictMode(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_STRICT_MODE, enabled).apply()
    }

    fun strictTarget(context: Context): Int =
        prefs(context).getInt(KEY_STRICT_TARGET, DEFAULT_TARGET).coerceIn(TARGET_RANGE.first, TARGET_RANGE.last)

    fun setStrictTarget(context: Context, count: Int) {
        prefs(context).edit()
            .putInt(KEY_STRICT_TARGET, count.coerceIn(TARGET_RANGE.first, TARGET_RANGE.last))
            .apply()
    }
}
