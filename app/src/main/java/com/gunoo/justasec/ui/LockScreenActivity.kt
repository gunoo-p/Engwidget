package com.gunoo.justasec.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gunoo.justasec.data.QuizSettings
import com.gunoo.justasec.data.StudyStats
import com.gunoo.justasec.data.WordRepository
import com.gunoo.justasec.service.CallGuard
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LockScreenActivity : ComponentActivity() {

    private val resetSignal = mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 잠금 해제 없이 키가드 위에 표시
        setShowWhenLocked(true)
        setContent {
            val signal by resetSignal
            key(signal) {
                QuizLockScreen(onClose = { finish() })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        // singleInstance라 화면이 꺼졌다 켜져도 이 Activity가 재사용된다.
        // key()로 감싼 컴포저블을 통째로 리셋해 이전 문제/선택 상태가 남지 않게 한다.
        resetSignal.value++
    }
}

private const val EMERGENCY_HOLD_MS = 5000L
private const val CLOCK_REFRESH_MS = 15_000L
private const val CALL_CHECK_MS = 1000L

@Composable
fun QuizLockScreen(onClose: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val strictMode = remember { QuizSettings.isStrictMode(context) }
    val strictTarget = remember { QuizSettings.strictTarget(context) }

    var index by remember { mutableIntStateOf(WordRepository.nextIndex(context)) }
    var selected by remember { mutableStateOf<String?>(null) }
    var solvedCount by remember { mutableIntStateOf(0) }
    var todayCount by remember { mutableIntStateOf(StudyStats.todayCount(context)) }

    val entry = remember(index) { WordRepository.wordAt(context, index) }
    val options = remember(index) { (entry.choices + entry.meaning).shuffled(Random(index)) }
    val canClose = !strictMode || solvedCount >= strictTarget

    // 강제 모드에서 뒤로가기(버튼/제스처/predictive back 전부 포함)를 무시.
    // 홈/최근앱은 시스템 전용 키라 여기서 막을 수 없지만, 잠금 상태에서는
    // 시스템이 런처 진입 자체를 막아주므로 사실상 무력화된다.
    BackHandler(enabled = strictMode && !canClose) {
        Toast.makeText(context, "${strictTarget - solvedCount}문제 더 풀어야 닫혀요", Toast.LENGTH_SHORT).show()
    }

    // 잠금화면 시계. 분 단위로 갱신되면 충분하지만 여유 있게 15초마다 갱신.
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(CLOCK_REFRESH_MS)
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }

    // 전화(벨소리/통화 중)가 감지되면 모드 상관없이 즉시 닫아 시스템 전화 화면이 보이게 한다.
    LaunchedEffect(Unit) {
        while (true) {
            if (CallGuard.isCallActive(context)) {
                onClose()
                break
            }
            delay(CALL_CHECK_MS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(QuizBg)
            .safeDrawingPadding() // 상태바/내비게이션바 영역 피하기
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(64.dp))

        // 시계 (잠금화면 느낌). 5초간 꾹 누르면 강제 모드에서도 즉시 탈출 가능
        // (전화 등 긴급 상황에 사용자를 가두지 않기 위한 타협 불가 안전장치).
        Text(
            text = currentTime,
            color = QuizTxtPrimary,
            fontSize = 48.sp,
            fontWeight = FontWeight.Light,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        val holdJob = scope.launch {
                            delay(EMERGENCY_HOLD_MS)
                            Toast.makeText(context, "긴급 탈출 사용됨", Toast.LENGTH_SHORT).show()
                            onClose()
                        }
                        tryAwaitRelease()
                        holdJob.cancel()
                    }
                )
            },
        )

        Spacer(Modifier.height(48.dp))

        WordQuizCard(
            entry = entry,
            options = options,
            selected = selected,
            onSelect = {
                selected = it
                solvedCount++
                StudyStats.recordSolved(context)
                todayCount = StudyStats.todayCount(context)
            },
        )

        Spacer(Modifier.weight(1f))

        if (todayCount > 0) {
            Text(
                text = "오늘 ${todayCount}개 풀었어요",
                color = QuizTxtSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(4.dp))
        }

        if (strictMode) {
            Text(
                text = "$solvedCount / $strictTarget 문제 풀면 닫을 수 있어요",
                color = QuizTxtSecondary,
                fontSize = 12.sp,
            )
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            QuizBottomButton(text = "다음 단어") {
                selected = null
                index = WordRepository.nextIndex(context)
            }
            if (canClose) {
                Spacer(Modifier.width(12.dp))
                QuizBottomButton(text = "닫기", onClick = onClose)
            }
        }
    }
}
