# GuideKit

Reusable Compose Multiplatform coachmark overlay.

GuideKit owns step navigation internally. The host app provides the steps, target bounds, and completion callbacks.

## Usage

```kotlin
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.guidekit.GuideKit
import io.github.guidekit.GuideKitAnchor
import io.github.guidekit.GuideKitArrowConfig
import io.github.guidekit.GuideKitArrowHead
import io.github.guidekit.GuideKitArrowLineStyle
import io.github.guidekit.GuideKitArrowStroke
import io.github.guidekit.GuideKitAutoScrollConfig
import io.github.guidekit.GuideKitInstructionBoxShadow
import io.github.guidekit.GuideKitInstructionBoxStyle
import io.github.guidekit.GuideKitStep
import io.github.guidekit.GuideKitStyle
import io.github.guidekit.GuideKitTargetHighlightShape
import io.github.guidekit.GuideKitTargetHighlightStroke
import io.github.guidekit.GuideKitTargetHighlightStyle
import androidx.compose.ui.graphics.StrokeCap

GuideKit(
    steps = listOf(
        GuideKitStep(
            targetBounds = firstTargetBounds,
            title = "Your day, at a glance.",
            description = "Important updates appear here when they are needed.",
            primaryButtonText = null, // Defaults to "Next", or "Got it" on the last step.
            descriptionHighlight = null,
            descriptionHighlights = emptyList(),
            instructionBottomPadding = 104.dp,
            arrowConfig = null, // Uses GuideKitStyle.arrowConfig.
            targetHighlight = null, // Uses GuideKitStyle.targetHighlight.
            instructionBox = null, // Uses GuideKitStyle.instructionBox.
            autoScroll = GuideKitAutoScrollConfig(
                enabled = true,
                minTopVisibleDistance = null, // Defaults to arrowConfig.minVisibleDistance + 1.dp.
                spacing = null, // Defaults to arrowConfig.minVisibleDistance + 1.dp.
            ),
        ),
        GuideKitStep(
            targetBounds = secondTargetBounds,
            title = "Help is always within reach.",
            description = "Press and hold for 3 seconds.",
            primaryButtonText = "Done",
            arrowConfig = GuideKitArrowConfig(
                from = GuideKitAnchor.BottomCenter,
                to = GuideKitAnchor.CenterLeft,
                arrowHead = GuideKitArrowHead.BothSides,
                lineStyle = GuideKitArrowLineStyle.Solid,
            ),
            targetHighlight = GuideKitTargetHighlightStyle(
                shape = GuideKitTargetHighlightShape.Circle,
            ),
            instructionBox = GuideKitInstructionBoxStyle(
                alignment = Alignment.BottomCenter,
            ),
            autoScroll = GuideKitAutoScrollConfig(enabled = false),
        ),
    ),
    modifier = Modifier,
    initialStepIndex = 0,
    showStepIndicator = true,
    style = GuideKitStyle(
        accentColor = Color(0xFF5ED5B3),
        overlayColor = Color.Black.copy(alpha = 0.68f),
        titleColor = null, // Defaults to MaterialTheme.colorScheme.onSurface.
        descriptionColor = null, // Defaults to MaterialTheme.colorScheme.onSurfaceVariant.
        highlightedDescriptionColor = null, // Defaults to accentColor.
        stepIndicatorActiveColor = null, // Defaults to accentColor.
        stepIndicatorInactiveColor = null, // Defaults to MaterialTheme.colorScheme.outlineVariant.
        primaryButtonContainerColor = null, // Defaults to accentColor.
        primaryButtonContentColor = Color(0xFF062D25),
        skipIconTint = null, // Defaults to MaterialTheme.colorScheme.onSurfaceVariant.
        arrowConfig = GuideKitArrowConfig(
            enabled = true,
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomCenter,
            curveSeed = 0,
            minVisibleDistance = 20.dp,
            lineStyle = GuideKitArrowLineStyle.Dashed,
            dashIntervalsPx = floatArrayOf(20f, 15f),
            dashPhasePx = 0f,
            strokes = listOf(
                GuideKitArrowStroke(widthPx = 9f, color = Color.Black.copy(alpha = 0.24f)),
                GuideKitArrowStroke(widthPx = 5.5f, color = null, alpha = 0.95f), // Defaults to GuideKitStyle.accentColor.
                GuideKitArrowStroke(widthPx = 1.7f, color = Color.White.copy(alpha = 0.62f)),
            ),
            strokeCap = StrokeCap.Round,
            arrowHead = GuideKitArrowHead.TargetSide,
            arrowHeadLengthPx = 38f,
            arrowHeadAngleDegrees = 30f,
            arrowHeadStrokes = listOf(
                GuideKitArrowStroke(widthPx = 9f, color = Color.Black.copy(alpha = 0.22f)),
                GuideKitArrowStroke(widthPx = 5.5f, color = null, alpha = 0.96f), // Defaults to GuideKitStyle.accentColor.
                GuideKitArrowStroke(widthPx = 1.6f, color = Color.White.copy(alpha = 0.55f)),
            ),
        ),
        targetHighlight = GuideKitTargetHighlightStyle(
            enabled = true,
            shape = GuideKitTargetHighlightShape.RoundedRect,
            cutoutEnabled = true,
            paddingPx = 10f,
            cornerRadius = 28.dp,
            glowStrokes = listOf(
                GuideKitTargetHighlightStroke(widthPx = 30f, alpha = 0.11f),
                GuideKitTargetHighlightStroke(widthPx = 22f, alpha = 0.18f),
                GuideKitTargetHighlightStroke(widthPx = 14f, alpha = 0.30f),
                GuideKitTargetHighlightStroke(widthPx = 8f, alpha = 0.45f),
            ),
            borderColor = null, // Defaults to GuideKitStyle.accentColor.
            borderWidthPx = 2.5f,
            innerBorderColor = Color.White.copy(alpha = 0.7f),
            innerBorderWidthPx = 1.2f,
            innerBorderInsetPx = 2f,
        ),
        instructionBox = GuideKitInstructionBoxStyle(
            alignment = Alignment.BottomCenter,
            outerPadding = null, // Defaults to start/end 18.dp and step.instructionBottomPadding.
            contentPadding = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
            fillMaxWidth = true,
            minWidth = null,
            maxWidth = null,
            minHeight = null,
            maxHeight = null,
            shape = RoundedCornerShape(30.dp),
            containerColor = null, // Defaults to MaterialTheme.colorScheme.surface.
            contentColor = null, // Defaults to MaterialTheme.colorScheme.onSurface.
            border = BorderStroke(1.dp, Color(0xFF5ED5B3).copy(alpha = 0.28f)),
            tonalElevation = 0.dp,
            shadowElevation = 26.dp,
            modifier = Modifier,
            shadow = GuideKitInstructionBoxShadow(
                elevation = 30.dp,
                ambientColor = Color.Black.copy(alpha = 0.42f),
                spotColor = Color.Black.copy(alpha = 0.42f),
            ),
        ),
    ),
    onStepChanged = { stepIndex -> },
    onScrollBy = { deltaPx -> scrollState.animateScrollBy(deltaPx) },
    onSkipped = onSkip,
    onFinished = onFinished,
)
```

All values shown above are optional defaults. `GuideKitStyle` defines screen-level defaults for arrows, target highlights, and the instruction box. A `GuideKitStep` can override any of them for that specific step; omitted step values inherit from `GuideKitStyle`.

Auto-scroll is enabled by default. GuideKit calculates the smallest scroll needed to keep the highlighted target clear of the instruction box. If scrolling upward, the target highlight is clamped so its top stays at least `minTopVisibleDistance` from the top edge. Set `autoScroll = GuideKitAutoScrollConfig(enabled = false)` on a step to disable it. The host app must provide `onScrollBy` when the page is scrollable.

## Sample App

A Compose Multiplatform sample app is available under `sample/`.

```bash
./gradlew :sample:composeApp:installDebug
```

The sample demonstrates target measurement, step-specific styling, arrow variants, rounded and circular highlights, auto-scroll, and completion callbacks.
