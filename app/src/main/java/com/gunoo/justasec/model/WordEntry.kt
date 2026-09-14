package com.gunoo.justasec.model

/** words.json 한 항목. 잠금화면 퀴즈 한 문제에 해당한다. */
data class WordEntry(
    val word: String,
    val meaning: String,
    val choices: List<String>, // 오답 3개
    val tip: String,
    val pron: String? = null, // 발음기호 (선택)
)
