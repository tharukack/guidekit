# GuideKit

GuideKit is a Compose Multiplatform coachmark overlay library for product tours, onboarding hints, and guided feature discovery.

GuideKit renders an overlay above your existing UI, highlights measured targets, draws arrows, shows an instruction card, and owns tour navigation internally. Your app provides the target bounds, step content, and completion callbacks.

Current version: `0.1.0`

## Platform Support

- Android through Compose Multiplatform
- iOS framework targets: `iosArm64`, `iosSimulatorArm64`
- Common Compose UI code through Kotlin Multiplatform

## Requirements

Recommended consumer setup:

- Kotlin `2.3.0+`
- Compose Multiplatform compatible with Kotlin `2.3.x`
- Android Gradle Plugin `8.x` for Android apps
- JDK `17+` for Android builds

If you use GuideKit as a published dependency, your app uses its own Gradle setup. The Gradle files in this repository are only used when building GuideKit from source or contributing to the library.

## Installation

### GitHub Packages

Add the GitHub Packages Maven repository to your root `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.github.com/tharukack/guidekit") {
            credentials {
                username = providers.gradleProperty("gpr.user").orNull
                    ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("gpr.key").orNull
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
```

Add credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN_WITH_PACKAGE_READ_ACCESS
```

Add GuideKit to the source set where you use it:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("io.github.tharukack.guidekit:guidekit:0.1.0")
        }
    }
}
```

If you publish the same artifact to Maven Central later, consumers can remove the GitHub Packages repository and keep the same dependency coordinate.

## Minimal Usage

GuideKit needs target bounds. In Compose, measure a target with `onGloballyPositioned` and `boundsInRoot()`.

```kotlin
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import io.github.tharukack.guidekit.GuideKit
import io.github.tharukack.guidekit.GuideKitStep

@Composable
fun ProductScreen() {
    val targetBounds = remember { mutableStateMapOf<String, Rect>() }

    Box(Modifier.fillMaxSize()) {
        PrimaryActionButton(
            modifier = Modifier.onGloballyPositioned { coordinates ->
                targetBounds["primaryAction"] = coordinates.boundsInRoot()
            },
        )

        GuideKit(
            steps = listOf(
                GuideKitStep(
                    targetBounds = targetBounds["primaryAction"],
                    title = "Create your first item",
                    description = "Tap here to start a new workflow.",
                ),
            ),
            onFinished = {
                // Persist completion or hide the tour.
            },
        )
    }
}
```

A `GuideKitStep` can be created before the target is measured. If `targetBounds` is temporarily `null`, GuideKit still shows the instruction card and updates once bounds are available.

## Showing And Hiding The Tour

Most apps control visibility outside GuideKit. GuideKit tells you when the user skips or finishes.

```kotlin
var showTour by remember { mutableStateOf(true) }

if (showTour) {
    GuideKit(
        steps = steps,
        onSkipped = { showTour = false },
        onFinished = { showTour = false },
    )
}
```

Use `onStepChanged` for analytics or screen state:

```kotlin
GuideKit(
    steps = steps,
    onStepChanged = { stepIndex ->
        analytics.track("guidekit_step", mapOf("index" to stepIndex))
    },
    onFinished = { showTour = false },
)
```

## Multiple Steps

GuideKit owns next, previous, skip, and finish navigation. Button text defaults to `Next`, and the last step defaults to `Got it`.

```kotlin
val steps = listOf(
    GuideKitStep(
        targetBounds = targetBounds["search"],
        title = "Search everything",
        description = "Find documents, people, and saved reports from one place.",
    ),
    GuideKitStep(
        targetBounds = targetBounds["filters"],
        title = "Refine results",
        description = "Use filters to narrow the results quickly.",
    ),
    GuideKitStep(
        targetBounds = targetBounds["export"],
        title = "Export when ready",
        description = "Download a clean report for sharing.",
        primaryButtonText = "Finish",
    ),
)
```

## Highlight Text In Descriptions

Use `descriptionHighlight` for one highlighted phrase or `descriptionHighlights` for multiple phrases.

```kotlin
GuideKitStep(
    targetBounds = targetBounds["sync"],
    title = "Sync across devices",
    description = "GuideKit can highlight key words inside the instruction text.",
    descriptionHighlight = "highlight key words",
)

GuideKitStep(
    targetBounds = targetBounds["reports"],
    title = "Review reports",
    description = "Track status, ownership, and deadlines from one card.",
    descriptionHighlights = listOf("status", "ownership", "deadlines"),
)
```

Highlighted text uses `GuideKitStyle.highlightedDescriptionColor`, or `accentColor` when no explicit highlighted color is provided.

## Global Styling

Use `GuideKitStyle` for screen-level defaults. Step-level styles override these defaults for that specific step.

```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.tharukack.guidekit.GuideKitStyle
import io.github.tharukack.guidekit.GuideKitInstructionBoxStyle
import io.github.tharukack.guidekit.GuideKitTargetHighlightStyle

GuideKit(
    steps = steps,
    style = GuideKitStyle(
        accentColor = Color(0xFFFFC857),
        overlayColor = Color.Black.copy(alpha = 0.68f),
        primaryButtonContentColor = Color(0xFF173B33),
        targetHighlight = GuideKitTargetHighlightStyle(
            cornerRadius = 28.dp,
            paddingPx = 10f,
            borderWidthPx = 3f,
        ),
        instructionBox = GuideKitInstructionBoxStyle(
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(1.dp, Color(0xFFFFC857).copy(alpha = 0.45f)),
        ),
    ),
    onFinished = { showTour = false },
)
```

## Matching Highlight Radius To Your UI

GuideKit does not inspect the target composable's shape. Provide the same radius externally when your target has rounded corners.

```kotlin
private val HeroCardRadius = 34.dp

Card(
    modifier = Modifier.onGloballyPositioned { coordinates ->
        targetBounds["hero"] = coordinates.boundsInRoot()
    },
    shape = RoundedCornerShape(HeroCardRadius),
) {
    // Card content.
}

GuideKitStep(
    targetBounds = targetBounds["hero"],
    title = "Start here",
    description = "This highlight uses the same corner radius as the card.",
    targetHighlight = GuideKitTargetHighlightStyle(
        cornerRadius = HeroCardRadius,
        paddingPx = 10f,
    ),
)
```

## Circular Highlights

Use circular highlights for icons, avatars, floating action buttons, and compact controls.

```kotlin
import io.github.tharukack.guidekit.GuideKitTargetHighlightShape
import io.github.tharukack.guidekit.GuideKitTargetHighlightStyle

GuideKitStep(
    targetBounds = targetBounds["avatar"],
    title = "Your profile",
    description = "Open account settings from this avatar.",
    targetHighlight = GuideKitTargetHighlightStyle(
        shape = GuideKitTargetHighlightShape.Circle,
        paddingPx = 16f,
    ),
)
```

For the best visual match, make the target itself square/circular:

```kotlin
Box(
    modifier = Modifier
        .size(56.dp)
        .clip(CircleShape)
        .onGloballyPositioned { coordinates ->
            targetBounds["avatar"] = coordinates.boundsInRoot()
        },
)
```

## Custom Glow And Borders

You can customize glow strokes, border color, and inner border independently.

```kotlin
GuideKitStep(
    targetBounds = targetBounds["importantAction"],
    title = "Important action",
    description = "Use custom highlight styling when a step needs more emphasis.",
    targetHighlight = GuideKitTargetHighlightStyle(
        paddingPx = 14f,
        cornerRadius = 24.dp,
        glowStrokes = listOf(
            GuideKitTargetHighlightStroke(widthPx = 36f, alpha = 0.10f, color = Color(0xFF4DA3FF)),
            GuideKitTargetHighlightStroke(widthPx = 18f, alpha = 0.28f, color = Color(0xFF4DA3FF)),
            GuideKitTargetHighlightStroke(widthPx = 7f, alpha = 0.58f, color = Color(0xFF4DA3FF)),
        ),
        borderColor = Color(0xFF4DA3FF),
        borderWidthPx = 3f,
        innerBorderColor = Color.White.copy(alpha = 0.72f),
    ),
)
```

Disable the transparent cutout while keeping the glow and border:

```kotlin
GuideKitTargetHighlightStyle(
    cutoutEnabled = false,
)
```

Disable target highlighting for a step:

```kotlin
GuideKitTargetHighlightStyle(
    enabled = false,
)
```

## Arrow Configuration

Arrows are enabled by default. Configure direction with anchors and choose solid or dashed lines.

```kotlin
import androidx.compose.ui.graphics.StrokeCap
import io.github.tharukack.guidekit.GuideKitAnchor
import io.github.tharukack.guidekit.GuideKitArrowConfig
import io.github.tharukack.guidekit.GuideKitArrowHead
import io.github.tharukack.guidekit.GuideKitArrowLineStyle
import io.github.tharukack.guidekit.GuideKitArrowStroke

GuideKitStep(
    targetBounds = targetBounds["cta"],
    title = "Take action",
    description = "The arrow can point from the instruction card to the target.",
    arrowConfig = GuideKitArrowConfig(
        from = GuideKitAnchor.TopRight,
        to = GuideKitAnchor.CenterRight,
        lineStyle = GuideKitArrowLineStyle.Solid,
        strokeCap = StrokeCap.Round,
        arrowHead = GuideKitArrowHead.TargetSide,
        strokes = listOf(
            GuideKitArrowStroke(widthPx = 10f, color = Color.Black.copy(alpha = 0.20f)),
            GuideKitArrowStroke(widthPx = 5f, color = Color(0xFFFFC857)),
        ),
    ),
)
```

Supported anchors:

- `TopLeft`
- `TopCenter`
- `TopRight`
- `CenterLeft`
- `Center`
- `CenterRight`
- `BottomLeft`
- `BottomCenter`
- `BottomRight`

Supported arrow heads:

- `None`
- `TargetSide`
- `InstructionBoxSide`
- `BothSides`

Disable arrows globally or for one step:

```kotlin
GuideKitArrowConfig(enabled = false)
```

## Instruction Card Styling

The instruction card is a Material `Surface`. You can control alignment, padding, dimensions, shape, color, border, elevation, and shadow.

```kotlin
GuideKitStep(
    targetBounds = targetBounds["settings"],
    title = "Settings",
    description = "This step uses a narrower instruction card.",
    instructionBox = GuideKitInstructionBoxStyle(
        alignment = Alignment.BottomCenter,
        maxWidth = 420.dp,
        fillMaxWidth = false,
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFF8EC),
        contentColor = Color(0xFF173B33),
        shadow = GuideKitInstructionBoxShadow(
            elevation = 32.dp,
            ambientColor = Color.Black.copy(alpha = 0.34f),
            spotColor = Color.Black.copy(alpha = 0.34f),
        ),
    ),
)
```

The card alignment can be changed per step:

```kotlin
GuideKitInstructionBoxStyle(
    alignment = Alignment.TopCenter,
)
```

## Auto-scroll

Auto-scroll is enabled by default. GuideKit calculates the smallest scroll needed to keep the highlighted target clear of the instruction card.

For scrollable screens, pass `onScrollBy`:

```kotlin
val scrollState = rememberScrollState()

Column(
    modifier = Modifier.verticalScroll(scrollState),
) {
    // Screen content.
}

GuideKit(
    steps = steps,
    onScrollBy = { deltaPx -> scrollState.animateScrollBy(deltaPx) },
    onFinished = { showTour = false },
)
```

Tune scroll behavior per step:

```kotlin
GuideKitStep(
    targetBounds = targetBounds["deepTarget"],
    title = "Deep content",
    description = "GuideKit can scroll to keep this target visible.",
    autoScroll = GuideKitAutoScrollConfig(
        enabled = true,
        minTopVisibleDistance = 32.dp,
        spacing = 28.dp,
    ),
)
```

Disable auto-scroll for a step:

```kotlin
GuideKitAutoScrollConfig(enabled = false)
```

## Step Indicator

The step indicator is shown by default. Hide it when you want a simpler card:

```kotlin
GuideKit(
    steps = steps,
    showStepIndicator = false,
    onFinished = { showTour = false },
)
```

Customize indicator colors through `GuideKitStyle`:

```kotlin
GuideKitStyle(
    stepIndicatorActiveColor = Color(0xFFFFC857),
    stepIndicatorInactiveColor = Color(0xFFE6D6B9),
)
```

## Initial Step

Start from a specific step with `initialStepIndex`:

```kotlin
GuideKit(
    steps = steps,
    initialStepIndex = 2,
    onFinished = { showTour = false },
)
```

The index is clamped to the valid step range.

## Controller API

GuideKit includes a small navigation controller for non-UI state tests or custom orchestration.

```kotlin
val controller = GuideKitController(stepCount = 3)

when (val result = controller.next()) {
    is GuideKitNavigationResult.StepChanged -> println(result.index)
    GuideKitNavigationResult.Finished -> println("finished")
    GuideKitNavigationResult.Skipped -> println("skipped")
    GuideKitNavigationResult.NoSteps -> println("no steps")
}
```

Most apps do not need this controller directly. The `GuideKit` composable manages navigation internally.

## Complete Example

```kotlin
@Composable
fun DashboardTour() {
    val targetBounds = remember { mutableStateMapOf<String, Rect>() }
    val scrollState = rememberScrollState()
    var showTour by remember { mutableStateOf(true) }

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
        ) {
            DashboardHeader(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    targetBounds["header"] = coordinates.boundsInRoot()
                },
            )
            ReportsCard(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    targetBounds["reports"] = coordinates.boundsInRoot()
                },
            )
            HelpButton(
                modifier = Modifier.onGloballyPositioned { coordinates ->
                    targetBounds["help"] = coordinates.boundsInRoot()
                },
            )
        }

        if (showTour) {
            GuideKit(
                steps = listOf(
                    GuideKitStep(
                        targetBounds = targetBounds["header"],
                        title = "Dashboard overview",
                        description = "Your key metrics live here.",
                        targetHighlight = GuideKitTargetHighlightStyle(cornerRadius = 32.dp),
                    ),
                    GuideKitStep(
                        targetBounds = targetBounds["reports"],
                        title = "Reports",
                        description = "Open reports, export data, and review trends.",
                        descriptionHighlights = listOf("export", "trends"),
                    ),
                    GuideKitStep(
                        targetBounds = targetBounds["help"],
                        title = "Need help?",
                        description = "Open support from this button at any time.",
                        primaryButtonText = "Done",
                        targetHighlight = GuideKitTargetHighlightStyle(
                            shape = GuideKitTargetHighlightShape.Circle,
                            paddingPx = 14f,
                        ),
                    ),
                ),
                style = GuideKitStyle(
                    accentColor = Color(0xFFFFC857),
                    overlayColor = Color.Black.copy(alpha = 0.70f),
                ),
                onScrollBy = { deltaPx -> scrollState.animateScrollBy(deltaPx) },
                onSkipped = { showTour = false },
                onFinished = { showTour = false },
            )
        }
    }
}
```

## Sample App

A Compose Multiplatform sample app is available under `sample/`.

```bash
./gradlew :sample:composeApp:installDebug
```

The sample demonstrates:

- Target measurement
- Step-specific styling
- Arrow variants
- Rounded and circular highlights
- Radius matching for highlighted cards
- Auto-scroll
- Completion callbacks

## Building From Source

This repository uses the Gradle setup required to build and publish GuideKit itself.

```bash
./gradlew build
```

Build the Android sample:

```bash
./gradlew :sample:composeApp:installDebug
```

Build the iOS sample framework:

```bash
./gradlew :sample:composeApp:linkDebugFrameworkIosSimulatorArm64
```

## Versioning

GuideKit follows semantic versioning.

- Patch releases fix bugs without changing public API.
- Minor releases add backward-compatible features.
- Major releases may include breaking API or toolchain changes.

First release: `0.1.0`.

## License

GuideKit is released under the license in this repository.
