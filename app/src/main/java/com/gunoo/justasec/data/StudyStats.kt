package com.gunoo.justasec.data

import android.content.Context
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 날짜별로 몇 문제를 풀었는지 기록하는 카운터. Room 없이 SharedPreferences에
 * {"yyyy-MM-dd": count, ...} 형태의 JSON 하나만 저장하는 가벼운 구현.
 * "학습 통계"(v1.1+ 백로그의 이력/그래프)를 정식으로 붙이기 전까지의 임시가 아니라,
 * 이 캘린더 이력 자체가 v1 스코프로 들어온 것 — CLAUDE.md 참고.
 */
object StudyStats {
    private const val PREFS_NAME = "vocab_stats"
    private const val KEY_HISTORY = "history"

    private fun today(): String = dateKey(Date())

    private fun dateKey(date: Date): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(date)

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private fun loadHistory(context: Context): JSONObject {
        val raw = prefs(context).getString(KEY_HISTORY, null) ?: return JSONObject()
        return runCatching { JSONObject(raw) }.getOrDefault(JSONObject())
    }

    private fun saveHistory(context: Context, history: JSONObject) {
        prefs(context).edit().putString(KEY_HISTORY, history.toString()).apply()
    }

    /** 문제를 하나 풀 때(정답/오답 상관없이 선택하는 순간)마다 호출. */
    fun recordSolved(context: Context) {
        val history = loadHistory(context)
        val key = today()
        history.put(key, history.optInt(key, 0) + 1)
        saveHistory(context, history)
    }

    /** 오늘 몇 개 풀었는지. */
    fun todayCount(context: Context): Int = loadHistory(context).optInt(today(), 0)

    /** 특정 날짜("yyyy-MM-dd")에 몇 개 풀었는지. 캘린더 셀 렌더링용. */
    fun countOn(context: Context, dateKey: String): Int = loadHistory(context).optInt(dateKey, 0)

    /** 기록이 있는 전체 날짜 → 카운트 맵. */
    fun allCounts(context: Context): Map<String, Int> {
        val history = loadHistory(context)
        val map = LinkedHashMap<String, Int>()
        history.keys().forEach { key -> map[key] = history.optInt(key, 0) }
        return map
    }
}
