# AGENTS.md — 잠깐단어 (잠금화면 단어퀴즈)

## 프로젝트 개요

**앱 이름: 잠깐단어** (v1 스토어 제목: "잠깐단어 - 잠금화면 단어퀴즈 (영어)" — 일본어 팩이 실제로 들어간 뒤에 "(영어, 일본어)"로 갱신. 제목 30자 제한). 갤럭시 S26 울트라에서 개발 중인 **잠금화면 영단어 퀴즈 앱**. 화면을 켤 때마다 시스템 잠금화면 위에 토익 어휘 4지선다 퀴즈가 뜨고, 정답 확인 시 발음기호와 암기 꿀팁 한 줄을 보여준다. 넥슨플레이 잠금화면과 같은 오버레이 방식.

**목표: Google Play 정식 출시.** 수익보다 출시 경험이 목적이므로 v1 스코프는 최소로 유지한다. 광고 없음 (잠금화면 광고 정책 문제 없음).

- 스택: **순수 Kotlin** (Flutter 아님), Jetpack Compose, (보조로 Glance 위젯)
- 패키지: ✅ `com.gunoo.justasec`로 확정·리팩토링 완료 (namespace/applicationId 동일, 소스도 `com/gunoo/justasec/`로 이동됨)
- minSdk 26 / compileSdk·targetSdk 36

## 제품 철학 (모든 기능 판단의 기준)

**"안 하는 것보단 낫다."** 각 잡고 공부하는 앱이 아니라, 나태해진 사람이 폰을 여는 김에 가볍게 하나라도 외우게 하는 앱이다. 폰을 하루 수십 번 여는 기존 습관에 올라타는 마이크로러닝.

여기서 나오는 설계 원칙:
- **한 번에 한 문제.** "N문제 세션" 같은 각 잡는 구조 금지. (사용자가 스스로 설정한 강제 모드는 예외)
- **기본값은 닫기를 벌하지 않는다.** 급하게 폰 여는 사람을 붙잡으면 앱이 삭제된다. 닫기는 즉시, 죄책감 없이.
- **노출 자체가 학습이다.** 답을 안 골라도 단어가 눈에 스치면 절반은 성공. 문제·정답·꿀팁이 한눈에 빨리 읽히는 게 우선.
- **압박 장치 금지.** 스트릭 끊김 경고, 목표 미달 알림 같은 죄책감 유발 UX 금지. 긍정 피드백("오늘 7개 스침")까지만.
- 기능 추가 제안이 이 철학과 충돌하면 거절하거나 v1.1+ 백로그로 보낼 것.

## v1 스코프 (이것만 한다)

잠금화면 퀴즈 + 셔플 덱 출제 + 토익 단어 약 1,200개 + **설정(순한맛/강제 모드)**. 오답 추적/통계/Room/원격 단어 업데이트/TTS는 전부 v1.1 이후. Glance 위젯은 v1에서 제외 검토 (기능이 둘이면 심사 소명·테스트 부담 두 배).

## 아키텍처

1. **잠금화면 오버레이 (메인)** — `LockScreenService`(FGS, specialUse)가 `ACTION_SCREEN_ON`을 동적 등록으로 감지 → `KeyguardManager.isKeyguardLocked` + `Settings.canDrawOverlays` 확인 후 `LockScreenActivity`(`setShowWhenLocked(true)`) 실행. 닫으면 밑의 진짜 키가드가 나타남. 잠금 자체는 건드리지 않는다.
2. **앱 직접 실행 (보조 진입점)** — `MainActivity`는 하단 탭 2개(학습/설정) 구조. **"학습" 탭이 기본값이자 앱의 홈**이라 앱 아이콘을 눌러 들어가도 바로 퀴즈(`StudyScreen`)가 뜬다. "설정" 탭에 권한 3단계 + 모드 설정 + 서비스 on/off가 있다. 잠금화면 퀴즈(`LockScreenActivity`)와 인앱 학습 탭(`StudyScreen`)은 같은 `WordQuizCard` 컴포넌트를 공유 — 시계/강제모드/뒤로가기차단/긴급탈출은 잠금화면 세션에만 있는 로직이라 `StudyScreen`엔 없음(그냥 자유롭게 탭 전환).
3. **홈/잠금화면 위젯 (보조)** — Glance 기반 `VocabWidget`. v1 제외 후보.

데이터는 `assets/words.json`. `WordRepository`가 로드/캐시. 몇천 개 규모여도 JSON 번들로 충분 (압축 후 수백 KB, 파싱 수십 ms).

## 파일 구조 (app/src/main)

패키지를 레이어별로 분리했다 (model / data / service / ui). manifest의 컴포넌트 이름도 서브패키지 포함 상대경로(`.ui.MainActivity` 등)로 지정돼 있으니 새 컴포넌트 추가 시 유의.

```
java/com/gunoo/justasec/
├── model/
│   └── WordEntry.kt        # words.json 한 항목의 데이터 클래스 (word, meaning, choices[3], tip, pron?)
├── data/
│   ├── WordRepository.kt   # words.json 로드/캐시 + nextIndex()로 셔플 덱 출제
│   ├── QuizSettings.kt     # 순한맛/강제모드 + 강제모드 문제 수 SharedPreferences 저장
│   └── StudyStats.kt       # 날짜별 풀이 카운트 {"yyyy-MM-dd": count} JSON을 SharedPreferences에 저장 (Room 없이 가볍게)
├── service/
│   ├── LockScreenService.kt # FGS(specialUse). SCREEN_ON 감지 → 퀴즈 실행. START_STICKY. 실행 상태를 SharedPreferences에 기록
│   └── BootReceiver.kt      # BOOT_COMPLETED → 서비스 재시작
├── ui/
│   ├── QuizTheme.kt            # 퀴즈 화면 공용 색상 팔레트 (잠금화면/학습탭/기록탭 공유)
│   ├── WordQuizCard.kt         # 단어+발음기호+4지선다+결과/꿀팁 공용 컴포저블 (+ QuizBottomButton)
│   ├── MainActivity.kt         # 하단 탭(학습/기록/설정) 껍데기 + 설정 탭 내용(권한 3단계 + 모드 설정 + 서비스 on/off) + 앱 전체 컬러스킴
│   ├── StudyScreen.kt          # "학습" 탭. 세션 개념 없는 자유 학습용 WordQuizCard 래퍼
│   ├── HistoryScreen.kt        # "기록" 탭. 월별 캘린더로 날짜별 풀이 수 표시 (색 진하기로만, 스트릭 경고 없음)
│   └── LockScreenActivity.kt   # Compose 퀴즈 화면(잠금화면 전용). 시계+WordQuizCard+강제모드/긴급탈출
├── VocabWidget.kt           # Glance 위젯 (v1 제외 후보, 아직 미구현 — 만들 경우 ui/widget 등으로 배치)
└── WordRotationWorker.kt    # 위젯용 2시간 주기 단어 교체 (위젯 제외 시 같이 제거, 아직 미구현)
assets/words.json            # 274/1200 단어 누적 중 (1차 배치 완료)
res/xml/vocab_widget_info.xml  # 아직 미구현
AndroidManifest.xml
```

## ⚠️ 빌드 환경 주의 (AGP 9 — 이미 한 번 삽질한 부분)

- **AGP 9.2.1 = built-in Kotlin.** `org.jetbrains.kotlin.android` 플러그인을 절대 추가하지 말 것 → `Cannot add extension with name 'kotlin'` 에러. toml에 kotlin-android alias도 없어야 정상.
- `kotlinOptions {}` 블록 사용 금지. jvmTarget은 `compileOptions` targetCompatibility(17)를 자동으로 따라감.
- Compose는 `org.jetbrains.kotlin.plugin.compose`만 별도 적용. toml: `kotlin = "2.3.21"`, `kotlin-compose = { id = ..., version.ref = "kotlin" }`. `composeOptions {}` 불필요.
- app 플러그인 블록은 alias 두 줄만: `alias(libs.plugins.android.application)` + `alias(libs.plugins.kotlin.compose)`. id()와 alias 중복 선언 금지.
- targetSdk 35+ = edge-to-edge 강제. 전체화면 UI에는 `safeDrawingPadding()` 필수.

## 다음 작업 (우선순위 순)

### A. 코드 수정 (코드 리뷰에서 확정된 것)

1. ✅ **완료 — 출제를 셔플 덱 방식으로.** `WordRepository.nextIndex(context)` 추가 (SharedPreferences `vocab_deck`에 순서/위치 저장, 덱 소진 시 재셔플). `LockScreenActivity` 랜덤 뽑기 두 곳 교체 완료. `singleInstance` 재사용 문제는 Activity의 `resetSignal` state + `onNewIntent` + `key(signal){}`로 화면 전체 상태 리셋해서 해결.
2. ✅ **완료 — MainActivity 서비스 상태 실측.** `LockScreenService.onCreate`/`onDestroy`(+`stop()`)에서 SharedPreferences(`vocab_service_state`)에 기록, MainActivity는 초기값 읽기 + `ON_RESUME`마다 재확인.
3. ✅ **완료 — 오버레이 권한 없이 켜기 시도 시 피드백.** Toast 안내 추가.
4. ✅ **완료 — 잠금화면 시계 갱신.** `LaunchedEffect` + 15초 `delay` 루프.
5. **applicationId 변경**: ✅ 완료 (`com.gunoo.justasec`, namespace 포함 패키지 리팩토링까지 반영, 빌드 확인됨). **앱 아이콘**: ⏳ 보류 — 사용자가 직접 나중에 제작 예정(현재 시스템 리소스 아이콘 유지 중).
6. ✅ **완료(선택 항목) — MainActivity `safeDrawingPadding()` 추가.**
7. ✅ **완료 — 설정 + 강제 모드.** `QuizSettings`(SharedPreferences `vocab_settings`)로 순한맛/강제모드 + 문제 수(1~10) 저장. MainActivity에 모드 선택 UI(라디오 카드 + 슬라이더) 추가. LockScreenActivity는:
   - **순한맛 (기본값)**: 닫기 버튼 항상 표시
   - **강제 모드**: 목표 문제 수 풀기 전엔 닫기 버튼 숨김, "N/M 문제 풀면 닫을 수 있어요" 진행도 표시
   - **뒤로가기 차단**: `BackHandler(enabled = strictMode && !canClose)`로 무시, 토스트 "N문제 더 풀어야 닫혀요"
   - **긴급 탈출**: 시계 텍스트 5초 홀드 → 모드 무관 즉시 닫힘 + "긴급 탈출 사용됨" 토스트
   - ⚠️ **미완료 항목**: 실기기 테스트(홈/최근앱/뒤로가기 제스처/전원 껐다켜기로 강제 모드 우회 가능한지)는 코드로 할 수 없는 부분이라 아직 검증 안 됨 — 실기기 확보 후 체크리스트로 진행 필요

### B. 단어 데이터 생성 ← **Codex가 직접 수행**

`assets/words.json`에 토익 빈출 어휘를 **250개 안팎 배치로 나눠 생성·누적**한다. 목표 총량 약 1,200개 (교재 한 권급. 그 이상은 빈출 효율 급감).

**진행 상황: 274/1200 (1차 배치 262개 완료, 기존 12개 포함).** 다음 배치부터는 기존 단어 목록 읽고 중복 제외 후 이어서 진행.

형식 (기존 스키마 유지):
```json
{
  "word": "reimburse",
  "pron": "/ˌriːɪmˈbɜːrs/",
  "meaning": "환급하다, 배상하다",
  "choices": ["연기하다", "감독하다", "합병하다"],
  "tip": "re(다시) + im + burse(지갑) → 지갑에 다시 넣어주다"
}
```

생성 규칙:
- 난이도 배분: 중급(700~800) 위주 60% / 기초 20% / 고득점(900+) 20%
- 토익 빈출 영역 중심: 회의·계약·배송·채용·재무·마케팅·출장 등 비즈니스 어휘
- `choices`는 오답 3개. 품사를 정답 뜻과 맞추고, 너무 뻔하거나(반의어) 헷갈릴 여지 없는 것 금지. 같은 배치 내 다른 단어의 뜻을 재활용하면 좋음
- `tip`은 한 줄: 어원 분해, 연상법, 혼동어 구분(adopt/adapt류) 중 하나
- `pron`은 미국식 IPA
- **배치 간 중복 단어 금지** — 생성 전 기존 words.json의 word 목록을 읽고 제외할 것
- 특정 교재(해커스/파고다 등) 리스트를 재현하지 말 것 (편집저작권). 독자 선별이어야 함
- 배치 완료 시마다 JSON 유효성 검사 (파싱 + 필드 누락 + choices 3개 확인) 스크립트 실행

### C. 배포 준비

- 개인정보처리방침 한 페이지 (수집 데이터 없음). GitHub Pages 호스팅
- 스토어 등록정보: 스크린샷, 설명. 앱 이름은 **잠깐단어** (res/values/strings.xml `app_name` 반영). v1 제목에 일본어를 미리 넣지 말 것 (허위 기재). 이름·설명에 "TOEIC/토익" 넣지 말 것 (ETS 상표) — 설명에 "토익 수준 비즈니스 어휘" 정도의 사실적 언급은 가능
- applicationId ✅ **확정 및 적용 완료: `com.gunoo.justasec`** (namespace도 동일, 패키지 리팩토링 완료, 빌드 검증됨) — 첫 업로드 후 변경 불가하니 이후로도 재확인 필요
- FGS specialUse 용도 소명 문구 + 시연 영상 준비
- 키스토어 생성 → AAB 빌드 (`./gradlew bundleRelease`) → Play Console
- 신규 개인 계정: **비공개 테스트 12명 × 14일 연속** 후 프로덕션 신청. 내부 테스트 트랙은 14일에 미포함. 테스트 기간 중 업데이트 1회 이상 배포 권장. 코드 완성을 기다리지 말고 테스트 트랙에 일찍 올려 14일 타이머부터 돌릴 것

## 배포 시 알아둘 정책/제약

- 잠금화면 광고 금지 정책 → 광고 없으므로 무관. 향후 수익화 시 재검토 필요
- Android 10+ 백그라운드 Activity 제한은 오버레이 권한 보유로 우회하는 구조. 권한 꺼지면 조용히 동작 안 함
- One UI가 서비스를 죽일 수 있음 → 사용자 안내(배터리 "제한 없음") + 장기적으로 생존성 개선 검토
- AOD에는 개입 불가. 화면 켜진 직후부터만

## v1.1+ 백로그

오답 단어 우선 재출제(Room 전환 타이밍) / TTS 발음 / 스와이프 닫기 / 출제 빈도 조절(N회에 1번, 시간대) / 원격 단어 업데이트(GitHub raw JSON 캐시) / 서비스 생존성(WorkManager 주기 체크) / **일본어 단어팩** (설정에 언어 선택 추가 + `words_ja.json`. 스키마는 현행 그대로 사용 가능 — word에 単語, pron에 요미가나. 추가 시 스토어 제목을 "(영어, 일본어)"로 갱신)

~~학습 통계(오늘 카운터 + 월별 캘린더 이력)~~ → **v1으로 앞당겨서 완료.** `StudyStats`(날짜별 카운트, SharedPreferences JSON) + "기록" 탭(`HistoryScreen`, 월별 캘린더 뷰)으로 반영됨. 애초에 Room을 예상했던 항목인데 SharedPreferences 하나로 충분해서 그렇게 감. 이 이상(주간 리포트, 요일별 통계 등)은 필요해지면 그때 백로그로.