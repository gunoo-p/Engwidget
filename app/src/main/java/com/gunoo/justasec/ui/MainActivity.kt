package com.gunoo.justasec.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gunoo.justasec.data.QuizSettings
import com.gunoo.justasec.service.LockScreenService

// 퀴즈 화면(QuizTheme)과 같은 팔레트를 앱 전체 Material 테마에도 적용.
// 기본값(연보라 Material You 톤)이 QuizTheme의 블루 브랜딩과 안 맞아서 통일함.
private val AppColorScheme = lightColorScheme(
    primary = QuizAccent,
    onPrimary = Color.White,
    primaryContainer = QuizAccent.copy(alpha = 0.15f),
    onPrimaryContainer = QuizAccentDeep,
    secondary = QuizAccentDeep,
    onSecondary = Color.White,
    secondaryContainer = QuizAccent.copy(alpha = 0.15f),
    onSecondaryContainer = QuizAccentDeep,
    background = QuizBg,
    onBackground = QuizTxtPrimary,
    surface = QuizCard,
    onSurface = QuizTxtPrimary,
    surfaceVariant = QuizDivider,
    onSurfaceVariant = QuizTxtSecondary,
    outline = QuizDivider,
    outlineVariant = QuizDivider,
    error = QuizWrong,
    onError = Color.White,
)

class MainActivity : ComponentActivity() {

    private val notifPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = AppColorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        onRequestNotification = {
                            if (Build.VERSION.SDK_INT >= 33) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        },
                        onRequestOverlay = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:$packageName")
                                )
                            )
                        },
                        onRequestBattery = {
                            val pm = getSystemService(PowerManager::class.java)
                            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                                startActivity(
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:$packageName")
                                    )
                                )
                            }
                        },
                        onStartService = { LockScreenService.start(this) },
                        onStopService = { LockScreenService.stop(this) },
                        hasOverlay = { Settings.canDrawOverlays(this) },
                    )
                }
            }
        }
    }
}

/**
 * 하단 탭 3개: "학습"(기본, 앱을 열면 바로 퀴즈) / "기록"(월별 캘린더) / "설정"(권한/모드/서비스 on-off).
 * 잠금화면을 안 켜놔도 앱만 열면 바로 단어를 볼 수 있게 하는 게 목적이라 학습 탭이 기본값.
 */
@Composable
private fun HomeScreen(
    onRequestNotification: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestBattery: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    hasOverlay: () -> Boolean,
) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    val itemColors = NavigationBarItemDefaults.colors(
        selectedIconColor = QuizAccentDeep,
        selectedTextColor = QuizAccentDeep,
        indicatorColor = QuizAccent.copy(alpha = 0.15f),
        unselectedIconColor = QuizTxtSecondary,
        unselectedTextColor = QuizTxtSecondary,
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = QuizCard) {
                NavigationBarItem(
                    selected = tab == 0,
                    onClick = { tab = 0 },
                    icon = { Icon(Icons.Filled.Home, contentDescription = "학습") },
                    label = { Text("학습") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = tab == 1,
                    onClick = { tab = 1 },
                    icon = { Icon(Icons.Filled.DateRange, contentDescription = "기록") },
                    label = { Text("기록") },
                    colors = itemColors,
                )
                NavigationBarItem(
                    selected = tab == 2,
                    onClick = { tab = 2 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "설정") },
                    label = { Text("설정") },
                    colors = itemColors,
                )
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (tab == 0) {
                StudyScreen()
            } else if (tab == 1) {
                HistoryScreen()
            } else {
                SettingsScreen(
                    onRequestNotification = onRequestNotification,
                    onRequestOverlay = onRequestOverlay,
                    onRequestBattery = onRequestBattery,
                    onStartService = onStartService,
                    onStopService = onStopService,
                    hasOverlay = hasOverlay,
                )
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onRequestNotification: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestBattery: () -> Unit,
    onStartService: () -> Unit,
    onStopService: () -> Unit,
    hasOverlay: () -> Boolean,
) {
    val context = LocalContext.current

    // 서비스 on/off 상태는 UI 로컬 변수가 아니라 LockScreenService가 onCreate/onDestroy에서
    // 기록해두는 실제 상태를 읽는다. 화면에 돌아올 때마다(ON_RESUME) 다시 읽어 최신화한다.
    var serviceRunning by remember { mutableStateOf(LockScreenService.isRunning(context)) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceRunning = LockScreenService.isRunning(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var strictMode by remember { mutableStateOf(QuizSettings.isStrictMode(context)) }
    var strictTarget by remember { mutableFloatStateOf(QuizSettings.strictTarget(context).toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text("⚙️ 잠금화면 자동 설정", fontSize = 22.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            "화면을 켤 때마다 잠금화면 위에 영단어 퀴즈가 뜨게 하려면 아래 순서대로 설정하세요.\n(설정 안 해도 '학습' 탭에서 언제든 공부할 수 있어요.)",
            fontSize = 14.sp,
        )
        Spacer(Modifier.height(24.dp))

        OutlinedButton(onClick = onRequestNotification, modifier = Modifier.fillMaxWidth()) {
            Text("1. 알림 권한 허용")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRequestOverlay, modifier = Modifier.fillMaxWidth()) {
            Text("2. 다른 앱 위에 표시 허용 (필수)")
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onRequestBattery, modifier = Modifier.fillMaxWidth()) {
            Text("3. 배터리 최적화 제외")
        }

        Spacer(Modifier.height(28.dp))
        Text("모드 설정", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(
            "급하게 폰을 열 때 앱이 붙잡으면 안 되니, 기본은 언제든 닫을 수 있는 순한맛이에요.",
            fontSize = 12.sp,
        )
        Spacer(Modifier.height(12.dp))

        ModeOption(
            title = "순한맛 (기본값)",
            description = "닫기 버튼이 항상 보이고, 언제든 바로 닫을 수 있어요.",
            selected = !strictMode,
            onClick = {
                strictMode = false
                QuizSettings.setStrictMode(context, false)
            },
        )
        Spacer(Modifier.height(8.dp))
        ModeOption(
            title = "강제 모드",
            description = "설정한 문제 수를 풀기 전엔 닫기 버튼이 숨겨져요. (긴급 탈출은 항상 가능)",
            selected = strictMode,
            onClick = {
                strictMode = true
                QuizSettings.setStrictMode(context, true)
            },
        )

        if (strictMode) {
            Spacer(Modifier.height(12.dp))
            Text("문제 수: ${strictTarget.toInt()}개")
            Slider(
                value = strictTarget,
                onValueChange = {
                    strictTarget = it
                    QuizSettings.setStrictTarget(context, it.toInt())
                },
                valueRange = QuizSettings.TARGET_RANGE.first.toFloat()..QuizSettings.TARGET_RANGE.last.toFloat(),
                steps = QuizSettings.TARGET_RANGE.last - QuizSettings.TARGET_RANGE.first - 1,
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (serviceRunning) {
                    onStopService()
                    serviceRunning = false
                } else if (hasOverlay()) {
                    onStartService()
                    serviceRunning = true
                } else {
                    Toast.makeText(
                        context,
                        "먼저 '다른 앱 위에 표시' 권한을 허용해주세요",
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(if (serviceRunning) "퀴즈 잠금화면 끄기" else "4. 퀴즈 잠금화면 켜기")
        }
    }
}

@Composable
private fun ModeOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(
            1.dp,
            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        ),
        shape = MaterialTheme.shapes.medium,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(Modifier.width(4.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(description, fontSize = 12.sp)
            }
        }
    }
}
