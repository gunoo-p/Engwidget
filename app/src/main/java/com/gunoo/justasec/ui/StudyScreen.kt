package com.gunoo.justasec.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunoo.justasec.data.StudyStats
import com.gunoo.justasec.data.WordRepository
import kotlin.random.Random

/**
 * 앱을 직접 열었을 때 보이는 "학습" 탭. 잠금화면 퀴즈(LockScreenActivity)와 같은
 * WordQuizCard를 쓰지만, 세션 강제/닫기 개념이 없는 화면이라 시계·강제모드·긴급탈출은 없다.
 * 그냥 언제든 들어와서 한 문제씩 보고 넘기면 되는 화면.
 */
@Composable
fun StudyScreen() {
    val context = LocalContext.current

    var index by remember { mutableIntStateOf(WordRepository.nextIndex(context)) }
    var selected by remember { mutableStateOf<String?>(null) }
    var todayCount by remember { mutableIntStateOf(StudyStats.todayCount(context)) }

    val entry = remember(index) { WordRepository.wordAt(context, index) }
    val options = remember(index) { (entry.choices + entry.meaning).shuffled(Random(index)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuizBg)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        if (todayCount > 0) {
            Text(
                text = "오늘 ${todayCount}개 풀었어요",
                color = QuizTxtSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))
        } else {
            Spacer(Modifier.height(24.dp))
        }

        WordQuizCard(
            entry = entry,
            options = options,
            selected = selected,
            onSelect = {
                selected = it
                StudyStats.recordSolved(context)
                todayCount = StudyStats.todayCount(context)
            },
        )

        Spacer(Modifier.weight(1f))

        QuizBottomButton(text = "다음 단어") {
            selected = null
            index = WordRepository.nextIndex(context)
        }

        Spacer(Modifier.height(24.dp))
    }
}
