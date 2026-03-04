# AGENTS.md — Kairn

Kairn is an Android hiking/walking community app built with Kotlin and Jetpack Compose.
This file provides guidance for agentic coding tools operating in this repository.

---

## Team & workflow (must-follow)

### Team context
- We are a team of 4 developers working on this repository.
- The product ROADMAP is maintained in the root README (./README.md). Always consult it before planning work.

### Git workflow rules
- For every feature and every modification, create at least one commit (no uncommitted feature-level changes).
- For any new feature (or when starting work on a feature), create a dedicated feature branch:
    - Branch naming: feature/<short-kebab-name> (or use the repo's existing convention if different).
- Do not commit directly to the default branch unless explicitly instructed.

### Definition of “feature”
A feature is any user-visible behavior change, new endpoint, new UI flow, new module, or any non-trivial refactor that alters behavior.

### Expected agent behavior
- Before implementing: check ./README.md (roadmap) and restate which roadmap item you are addressing.
- When starting a new feature: propose the feature branch name, then proceed on that branch.
- While implementing: make logically separated commits (e.g., scaffolding, core logic, tests, docs) when it improves reviewability.

## Project Overview

- **Platform:** Android (minSdk 34 / Android 14+, targetSdk 36)
- **Language:** Kotlin 2.0.21
- **UI:** Jetpack Compose + Material3
- **Build system:** Gradle with Kotlin DSL (`*.gradle.kts`) and version catalog (`gradle/libs.versions.toml`)
- **Module structure:** Single Android module (`:app`)
- **Package:** `com.example.kairn`

---

## Build Commands

All commands are run from the repository root using the Gradle wrapper.

```bash
# Assemble debug APK
./gradlew assembleDebug

# Assemble release APK
./gradlew assembleRelease

# Run all JVM unit tests
./gradlew test

# Run all instrumented (on-device) tests
./gradlew connectedAndroidTest

# Run a single JVM unit test class
./gradlew :app:testDebugUnitTest --tests "com.example.kairn.ExampleUnitTest"

# Run a single JVM unit test method
./gradlew :app:testDebugUnitTest --tests "com.example.kairn.ExampleUnitTest.addition_isCorrect"

# Run a single instrumented test class (requires connected device/emulator)
./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.kairn.ExampleInstrumentedTest

# Check for compilation errors without building APK
./gradlew compileDebugKotlin

# Clean build outputs
./gradlew clean
```

> There is no lint, detekt, or ktlint configuration yet. When added, run:
> `./gradlew lint` and `./gradlew detekt` / `./gradlew ktlintCheck`.

---

## Architecture

The target layered architecture (not yet fully implemented):

```
app/src/main/java/com/example/kairn/
├── ui/          # Compose screens, components, ViewModels
│   └── theme/   # Color, Type, Theme (already exists)
├── domain/      # Use cases, domain models, repository interfaces
└── data/        # Repository implementations, Room DAOs, network sources
```

- **UI layer:** Compose screens + `ViewModel` (no business logic in composables)
- **Domain layer:** Pure Kotlin use cases; no Android/framework dependencies
- **Data layer:** Repository pattern; Room for local persistence, Ktor/OkHttp + Supabase for remote
- **DI:** Hilt (not yet added; use `@HiltAndroidApp`, `@HiltViewModel`, `@Inject`)

Planned dependencies (per roadmap, not yet in `build.gradle.kts`):
- Navigation Compose, Hilt, Room + WorkManager, Ktor/OkHttp
- Supabase Kotlin SDK (Auth, PostgREST, Realtime, Storage)
- MapLibre (maps), Google Play Services Location (GPS)
- Kotlinx Serialization, Timber (logging)

---

## Dependency Management

All library versions and aliases live in `gradle/libs.versions.toml`.

- **Always** add new dependencies via the version catalog — never hardcode version strings directly in `build.gradle.kts`.
- Reference libraries as `libs.<alias>` in Gradle files.
- Use the Compose BOM (`platform(libs.androidx.compose.bom)`) for all Compose artifact versions.

```toml
# Example addition in libs.versions.toml
[versions]
hilt = "2.51.1"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

---

## Design System

### Color Palette

The app uses a nature-inspired color scheme. **Always use token names via MaterialTheme, never hard-code hex values.**

| Token | Name | Hex Value | Usage |
|---|---|---|---|
| `primary` | Primary | `#587b6c` | Primary brand color, main UI elements |
| `secondary` | Secondary | `#b5c696` | Secondary accents, highlights |
| `accent` | Accent | `#ba8c5e` | Call-to-action, emphasis |
| `background` | Background | `#ece7df` | App background, surfaces |
| `text` | Text | `#1d2622` | Primary text color |
| `textAccent` | Text Accent | `#7ca16a` | Secondary/accent text |

**Correct usage in Compose:**
```kotlin
// ✅ CORRECT - Use semantic tokens
Surface(color = MaterialTheme.colorScheme.primary) { }
Text(color = MaterialTheme.colorScheme.text)

// ❌ WRONG - Never hard-code colors
Surface(color = Color(0xFF587b6c)) { }
Text(color = Color(0xFF1d2622))
```

Define colors in `ui/theme/Color.kt`:
```kotlin
val Primary = Color(0xFF587b6c)
val Secondary = Color(0xFFb5c696)
// ... etc
```

Map to Material3 ColorScheme in `ui/theme/Theme.kt`:
```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    onBackground = Text,
    // ... etc
)
```

### Typography

The app uses two custom font families. **Always reference typography styles via MaterialTheme, never hard-code font properties.**

#### Typography Styles

| Token | Font Family | Weight | Size | Usage |
|---|---|---|---|---|
| `title` | Clash Display Variable | 600 | 44sp | Screen titles, major headings |
| `heading` | Clash Display Variable | 500 | 28sp | Section headers, card titles |
| `text` | Satoshi | 400 | 16sp | Body text, labels, descriptions |

**Correct usage in Compose:**
```kotlin
// ✅ CORRECT - Use semantic tokens
Text(
    text = "Welcome",
    style = MaterialTheme.typography.displayLarge // title token
)
Text(
    text = "Section",
    style = MaterialTheme.typography.headlineMedium // heading token
)
Text(
    text = "Body content",
    style = MaterialTheme.typography.bodyLarge // text token
)

// ❌ WRONG - Never hard-code typography
Text(
    text = "Welcome",
    fontSize = 44.sp,
    fontWeight = FontWeight.SemiBold,
    fontFamily = FontFamily(/* ... */)
)
```

**Setup instructions:**
1. Place custom font files in `app/src/main/res/font/`:
   - `clash_display_variable.ttf`
   - `satoshi_regular.ttf` (and other weights as needed)

2. Define typography in `ui/theme/Type.kt`:
```kotlin
private val ClashDisplay = FontFamily(
    Font(R.font.clash_display_variable, FontWeight.Medium),
    Font(R.font.clash_display_variable, FontWeight.SemiBold)
)

private val Satoshi = FontFamily(
    Font(R.font.satoshi_regular, FontWeight.Normal)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.SemiBold,
        fontSize = 44.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = ClashDisplay,
        fontWeight = FontWeight.Medium,
        fontSize = 28.sp,
        letterSpacing = 0.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = Satoshi,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        letterSpacing = 0.sp
    ),
)
```

3. Apply in `ui/theme/Theme.kt`:
```kotlin
MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
)
```

---

## Code Style

`kotlin.code.style=official` is set in `gradle.properties`. Follow the
[Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

### Naming

| Element | Convention | Example |
|---|---|---|
| Classes / objects / enums | `PascalCase` | `UserRepository`, `HikeStatus` |
| Composable functions | `PascalCase` | `HikeCard`, `ProfileScreen` |
| Functions / properties | `camelCase` | `fetchHikes()`, `currentUser` |
| Constants (`const val`) | `SCREAMING_SNAKE_CASE` | `MAX_RETRY_COUNT` |
| Preview composables | `<Name>Preview` | `HikeCardPreview` |
| Test methods | `snake_case` describing behavior | `fetchHikes_returnsEmptyListOnError` |

### Imports

- No wildcard imports (no `import com.example.kairn.*`).
- Order: Android/framework → third-party → project-local, separated by blank lines.
- Remove unused imports.

### Formatting

- Indentation: 4 spaces (no tabs).
- Max line length: 120 characters.
- Trailing commas in multi-line argument/parameter lists.
- Opening brace on the same line; no blank line after opening brace.

### Composables

- Every `@Composable` function must have `modifier: Modifier = Modifier` as a parameter (except internal/private helpers that intentionally don't need it).
- Keep composables stateless where possible; hoist state to the ViewModel.
- Name preview functions `@Preview @Composable fun <ComponentName>Preview()`.
- Use `@Preview(showBackground = true)` for screen-level previews.

```kotlin
@Composable
fun HikeCard(
    hike: Hike,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // ...
}

@Preview(showBackground = true)
@Composable
fun HikeCardPreview() {
    KairnTheme { HikeCard(hike = Hike.preview, onClick = {}) }
}
```

### Types

- Prefer non-nullable types; use `?` only when `null` is a meaningful value.
- Use Kotlin data classes for domain/UI models.
- Use `sealed class` / `sealed interface` for UI state and one-shot events.
- Avoid raw `Any` or unchecked casts.

```kotlin
sealed interface HikeListUiState {
    data object Loading : HikeListUiState
    data class Success(val hikes: List<Hike>) : HikeListUiState
    data class Error(val message: String) : HikeListUiState
}
```

### Error Handling

- Use Kotlin `Result<T>` or a custom `sealed class` (`Success` / `Error`) to propagate errors — never swallow exceptions silently.
- In repositories/use cases, catch exceptions at the data boundary and convert to domain errors.
- Never call `runBlocking` on the main thread.
- Use `Timber.e(throwable, "message")` for logging errors (once Timber is added).

```kotlin
suspend fun getHikes(): Result<List<Hike>> = runCatching {
    remoteDataSource.fetchHikes()
}
```

### Coroutines & Flow

- ViewModels use `viewModelScope`; repositories use injected `CoroutineDispatcher`.
- Expose UI state as `StateFlow<UiState>`, not `LiveData`.
- Collect flows in composables with `collectAsStateWithLifecycle()` (lifecycle-aware).
- Default dispatcher for CPU work: `Dispatchers.Default`; IO: `Dispatchers.IO`.

---

## Testing

### Unit Tests (`src/test/`)

- Framework: JUnit4 (`org.junit.Test`, `org.junit.Assert.*`)
- Mocking: MockK (not yet added; prefer it over Mockito for Kotlin)
- Coroutine testing: `kotlinx-coroutines-test` with `runTest`
- File naming: `<ClassUnderTest>Test.kt`, same package as the class

```kotlin
class HikeRepositoryTest {
    @Test
    fun `fetchHikes returns empty list when network fails`() = runTest {
        // arrange / act / assert
    }
}
```

### Instrumented Tests (`src/androidTest/`)

- Framework: `@RunWith(AndroidJUnit4::class)`, `InstrumentationRegistry`
- Compose UI tests: `ComposeTestRule` from `androidx.compose.ui.test.junit4`
- Use `createComposeRule()` for composable tests and `createAndroidComposeRule<MainActivity>()` for full-screen tests.

---

## Git Conventions

Commit messages follow the pattern `<type>: <short description>` (lowercase):

```
init: add hike list screen
feat: implement offline caching with Room
fix: correct GPS permission request flow
update: bump compose bom to 2025.01.00
refactor: extract HikeRepository interface
test: add unit tests for GetHikesUseCase
chore: update AGP to 9.1.0
```

Branch names: `feat/<topic>`, `fix/<topic>`, `chore/<topic>`.

---

## Key Files Reference

| File | Purpose |
|---|---|
| `app/build.gradle.kts` | App-level build config, dependencies |
| `gradle/libs.versions.toml` | Centralized version catalog |
| `gradle.properties` | JVM/Kotlin build flags |
| `app/src/main/java/com/example/kairn/MainActivity.kt` | App entry point |
| `app/src/main/java/com/example/kairn/ui/theme/` | Material3 theme (Color, Type, Theme) |
| `app/src/test/` | JVM unit tests |
| `app/src/androidTest/` | Instrumented / Compose UI tests |
