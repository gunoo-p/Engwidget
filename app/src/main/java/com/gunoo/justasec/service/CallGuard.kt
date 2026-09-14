package com.gunoo.justasec.service

import android.content.Context
import android.media.AudioManager

/**
 * 전화(벨소리/통화 중)를 이 앱의 잠금화면 퀴즈보다 우선시키기 위한 판단.
 * READ_PHONE_STATE 없이 AudioManager 모드로 판단 — 정확한 통화 상태 API는
 * targetSdk 29+부터 해당 permission이 있어야 값을 주기 때문에, 대신 통화 시
 * 시스템이 바꾸는 오디오 모드를 본다.
 * ponytail: 다른 앱이 같은 모드를 쓰는 극히 드문 오탐 가능 — 문제 되면 READ_PHONE_STATE로 교체.
 */
object CallGuard {
    fun isCallActive(context: Context): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return false
        return when (audioManager.mode) {
            AudioManager.MODE_RINGTONE,
            AudioManager.MODE_IN_CALL,
            AudioManager.MODE_IN_COMMUNICATION -> true
            else -> false
        }
    }
}
