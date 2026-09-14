package com.gunoo.justasec.ui

import androidx.compose.ui.graphics.Color

/**
 * 퀴즈 화면(잠금화면 오버레이 + 인앱 학습 탭)이 공유하는 색상.
 * 두 화면이 같은 컴포넌트를 쓰니 팔레트도 한 곳에서 관리한다.
 */
internal val QuizBg = Color(0xFFF7FAFC)         // 전체 배경 — 아주 연한 블루/화이트
internal val QuizCard = Color(0xFFFFFFFF)       // 카드 배경 — 흰색
internal val QuizAccent = Color(0xFF2563EB)     // 메인 컬러 — 차분한 블루
internal val QuizAccentDeep = Color(0xFF1D4ED8) // 메인 진한색 — Deep Blue
internal val QuizTxtPrimary = Color(0xFF172033) // 기본 글자 — 거의 검정
internal val QuizTxtSecondary = Color(0xFF64748B) // 보조 글자 — 차분한 회색
internal val QuizTip = Color(0xFFF59E0B)        // 암기 팁 — 따뜻한 옐로우
internal val QuizCorrect = Color(0xFF22C55E)    // 정답 — 그린
internal val QuizWrong = Color(0xFFEF4444)      // 오답 — 레드
internal val QuizDivider = Color(0xFFE2E8F0)    // 구분선 — 연한 회색
