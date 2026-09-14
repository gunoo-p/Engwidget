package com.gunoo.justasec.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunoo.justasec.model.WordEntry

/**
 * 단어 + 발음기호 + 4지선다 + (정답 후) 결과·꿀팁을 보여주는 퀴즈 카드.
 * 잠금화면 오버레이(LockScreenActivity)와 인앱 학습 탭(StudyScreen)이 함께 쓰는 핵심 컴포넌트.
 * 시계, 강제 모드, 닫기/긴급탈출 같은 화면별 로직은 이 컴포넌트 밖에서 처리한다.
 */
@Composable
fun WordQuizCard(
    entry: WordEntry,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val answered = selected != null
    val isCorrect = selected == entry.meaning

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = entry.word,
            color = QuizAccentDeep,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
        )
        entry.pron?.let { pron ->
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .background(QuizCard, RoundedCornerShape(50))
                    .border(1.dp, QuizDivider, RoundedCornerShape(50))
                    .padding(horizontal = 14.dp, vertical = 6.dp),
            ) {
                Text(
                    text = pron,
                    color = QuizTxtSecondary,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace,
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        options.forEach { option ->
            val isAnswer = option == entry.meaning
            val isPicked = option == selected
            val bgColor = when {
                !answered -> QuizCard
                isAnswer -> QuizCorrect.copy(alpha = 0.12f)
                isPicked -> QuizWrong.copy(alpha = 0.12f)
                else -> QuizCard
            }
            val borderColor = when {
                !answered -> QuizDivider
                isAnswer -> QuizCorrect.copy(alpha = 0.5f)
                isPicked -> QuizWrong.copy(alpha = 0.5f)
                else -> QuizDivider
            }
            val marker = when {
                answered && isAnswer -> "✓"
                answered && isPicked -> "✗"
                else -> "○"
            }
            val markerColor = when {
                !answered -> QuizTxtSecondary.copy(alpha = 0.6f)
                isAnswer -> QuizCorrect
                isPicked -> QuizWrong
                else -> QuizTxtSecondary.copy(alpha = 0.3f)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .background(bgColor, RoundedCornerShape(14.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .clickable(enabled = !answered) { onSelect(option) }
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = marker, color = markerColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(14.dp))
                Text(text = option, color = QuizTxtPrimary, fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(20.dp))

        if (answered) {
            Text(
                text = if (isCorrect) "정답! ✅" else "오답 ❌  정답: ${entry.meaning}",
                color = if (isCorrect) QuizCorrect else QuizWrong,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "💡 ${entry.tip}",
                color = QuizTip,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
internal fun QuizBottomButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(QuizCard, RoundedCornerShape(20.dp))
            .border(1.dp, QuizDivider, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 28.dp, vertical = 12.dp),
    ) {
        Text(text = text, color = QuizAccentDeep, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
