package com.gunoo.justasec.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunoo.justasec.data.StudyStats
import java.time.LocalDate
import java.time.YearMonth

/**
 * "기록" 탭 — 월별 캘린더로 하루하루 몇 개 풀었는지 보여준다.
 * 죄책감 유발 요소(스트릭 끊김 경고 등) 없이 그냥 지나간 기록을 색 진하기로만 보여주는 정도.
 */
@Composable
fun HistoryScreen() {
    val context = LocalContext.current
    var yearMonth by remember { mutableStateOf(YearMonth.now()) }
    val counts = remember(yearMonth) { StudyStats.allCounts(context) }
    val today = remember { LocalDate.now() }
    val isCurrentMonth = yearMonth == YearMonth.from(today)

    val firstOfMonth = yearMonth.atDay(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // 일요일 시작 기준 (일=0 ... 토=6)
    val cells: List<LocalDate?> = buildList {
        repeat(leadingBlanks) { add(null) }
        for (day in 1..yearMonth.lengthOfMonth()) add(yearMonth.atDay(day))
        while (size % 7 != 0) add(null)
    }
    val weeks = cells.chunked(7)

    val monthTotal = (1..yearMonth.lengthOfMonth()).sumOf { day ->
        counts[yearMonth.atDay(day).toString()] ?: 0
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuizBg)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Text("학습 기록", color = QuizTxtPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { yearMonth = yearMonth.minusMonths(1) }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "이전 달", tint = QuizAccentDeep)
            }
            Text(
                text = "${yearMonth.year}년 ${yearMonth.monthValue}월",
                color = QuizTxtPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
            )
            IconButton(
                onClick = { yearMonth = yearMonth.plusMonths(1) },
                enabled = !isCurrentMonth,
            ) {
                Icon(
                    Icons.Filled.ArrowForward,
                    contentDescription = "다음 달",
                    tint = if (isCurrentMonth) QuizTxtSecondary.copy(alpha = 0.3f) else QuizAccentDeep,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("일", "월", "화", "수", "목", "금", "토").forEach { label ->
                Text(
                    text = label,
                    color = QuizTxtSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (date != null) {
                            DayCell(date = date, count = counts[date.toString()] ?: 0, isToday = date == today)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            text = if (monthTotal > 0) "이번 달 총 ${monthTotal}개 풀었어요" else "이번 달은 아직이에요",
            color = QuizTxtSecondary,
            fontSize = 13.sp,
        )
    }
}

@Composable
private fun DayCell(date: LocalDate, count: Int, isToday: Boolean) {
    // 색 진하기로 암시하지 않고 실제 개수를 숫자로 바로 보여준다.
    val bgColor = if (count > 0) QuizAccent.copy(alpha = 0.12f) else Color.Transparent
    val borderColor = if (isToday) QuizAccentDeep else QuizDivider

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(if (isToday) 1.5.dp else 1.dp, borderColor, RoundedCornerShape(10.dp)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = date.dayOfMonth.toString(), color = QuizTxtSecondary, fontSize = 10.sp)
        Text(
            text = if (count > 0) count.toString() else "",
            color = QuizAccentDeep,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
