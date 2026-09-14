package com.gunoo.justasec.data

import android.content.Context
import com.gunoo.justasec.model.WordEntry
import org.json.JSONArray

object WordRepository {

    @Volatile
    private var cache: List<WordEntry>? = null

    fun load(context: Context): List<WordEntry> {
        cache?.let { return it }
        synchronized(this) {
            cache?.let { return it }
            val json = context.assets.open("words_dev.json")
                .bufferedReader()
                .use { it.readText() }
            val arr = JSONArray(json)
            val list = buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val choicesArr = obj.getJSONArray("choices")
                    add(
                        WordEntry(
                            word = obj.getString("word"),
                            meaning = obj.getString("meaning"),
                            choices = List(choicesArr.length()) { c -> choicesArr.getString(c) },
                            tip = obj.getString("tip"),
                            pron = obj.optString("pron").takeIf { it.isNotBlank() },
                        )
                    )
                }
            }
            cache = list
            return list
        }
    }

    /** 현재 단어. index가 범위를 벗어나면 안전하게 순환 */
    fun wordAt(context: Context, index: Int): WordEntry {
        val words = load(context)
        return words[Math.floorMod(index, words.size)]
    }

    fun size(context: Context): Int = load(context).size

    /**
     * 셔플 덱 방식 출제. 전체 단어를 한 번씩 다 보여준 뒤에야 재셔플되므로
     * 완전 랜덤(Random.nextInt)과 달리 같은 단어가 금방 다시 나오는 체감이 없다.
     * 순서(order)와 위치(pos)는 SharedPreferences에 저장해 프로세스가 재시작돼도 이어진다.
     */
    fun nextIndex(context: Context): Int {
        val words = load(context)
        val prefs = context.getSharedPreferences(DECK_PREFS_NAME, Context.MODE_PRIVATE)

        val savedOrder = prefs.getString(KEY_ORDER, null)
            ?.split(",")
            ?.mapNotNull { it.toIntOrNull() }
            ?.takeIf { it.size == words.size }
        val savedPos = prefs.getInt(KEY_POS, 0)

        val (order, pos) = if (savedOrder != null && savedPos < savedOrder.size) {
            savedOrder to savedPos
        } else {
            // 덱 소진(또는 첫 실행, 단어 수 변경) → 재셔플
            words.indices.shuffled() to 0
        }

        val index = order[pos]
        prefs.edit()
            .putString(KEY_ORDER, order.joinToString(","))
            .putInt(KEY_POS, pos + 1)
            .apply()
        return index
    }

    private const val DECK_PREFS_NAME = "vocab_deck"
    private const val KEY_ORDER = "shuffle_order"
    private const val KEY_POS = "shuffle_pos"
}
