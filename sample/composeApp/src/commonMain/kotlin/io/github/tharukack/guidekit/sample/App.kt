package io.github.tharukack.guidekit.sample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.tharukack.guidekit.GuideKit
import io.github.tharukack.guidekit.GuideKitAnchor
import io.github.tharukack.guidekit.GuideKitArrowConfig
import io.github.tharukack.guidekit.GuideKitArrowHead
import io.github.tharukack.guidekit.GuideKitArrowLineStyle
import io.github.tharukack.guidekit.GuideKitArrowStroke
import io.github.tharukack.guidekit.GuideKitAutoScrollConfig
import io.github.tharukack.guidekit.GuideKitInstructionBoxShadow
import io.github.tharukack.guidekit.GuideKitInstructionBoxStyle
import io.github.tharukack.guidekit.GuideKitStep
import io.github.tharukack.guidekit.GuideKitStyle
import io.github.tharukack.guidekit.GuideKitTargetHighlightShape
import io.github.tharukack.guidekit.GuideKitTargetHighlightStroke
import io.github.tharukack.guidekit.GuideKitTargetHighlightStyle

private const val TargetHero = "hero"
private const val TargetProgress = "progress"
private const val TargetPrimaryAction = "primaryAction"
private const val TargetMetric = "metric"
private const val TargetHelp = "help"

private val HeroCardCornerRadius = 34.dp
private val HelpCardCornerRadius = 32.dp

@Composable
fun App() {
    MaterialTheme {
        GuideKitSampleScreen()
    }
}

@Composable
private fun GuideKitSampleScreen() {
    val targetBounds = remember { mutableStateMapOf<String, Rect>() }
    val scrollState = rememberScrollState()
    var showTour by remember { mutableStateOf(true) }
    var lastEvent by remember { mutableStateOf("Tour is ready") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sampleBackground()),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Header(
                modifier = Modifier.trackGuideTarget(TargetHero, targetBounds),
                onRestartTour = {
                    lastEvent = "Tour restarted"
                    showTour = true
                },
            )
            StatusStrip(
                modifier = Modifier.trackGuideTarget(TargetProgress, targetBounds),
            )
            FeatureGrid(
                primaryActionModifier = Modifier.trackGuideTarget(TargetPrimaryAction, targetBounds),
                metricModifier = Modifier.trackGuideTarget(TargetMetric, targetBounds),
            )
            HelpCard(
                modifier = Modifier.trackGuideTarget(TargetHelp, targetBounds),
            )
            Spacer(Modifier.height(260.dp))
            Text(
                text = lastEvent,
                color = Color(0xFF6B6255),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        if (showTour) {
            GuideKit(
                steps = guideSteps(targetBounds),
                style = sampleGuideStyle(),
                onScrollBy = { deltaPx -> scrollState.animateScrollBy(deltaPx) },
                onStepChanged = { index -> lastEvent = "Viewing step ${index + 1}" },
                onSkipped = {
                    showTour = false
                    lastEvent = "Tour skipped"
                },
                onFinished = {
                    showTour = false
                    lastEvent = "Tour finished"
                },
            )
        }
    }
}

@Composable
private fun Header(
    modifier: Modifier,
    onRestartTour: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HeroCardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF173B33)),
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "GuideKit",
                color = Color(0xFFFFF7EA),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "A Compose Multiplatform coachmark overlay with arrows, highlights, custom instruction cards, and auto-scroll.",
                color = Color(0xFFD7E9DC),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onRestartTour,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFC857),
                    contentColor = Color(0xFF173B33),
                ),
            ) {
                Text("Replay sample tour")
            }
        }
    }
}

@Composable
private fun StatusStrip(modifier: Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFFFFF8EC),
        border = BorderStroke(1.dp, Color(0xFFE6D6B9)),
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusPill("Step state", "Internal")
            StatusPill("Targets", "Measured")
            StatusPill("Scroll", "Automatic")
        }
    }
}

@Composable
private fun FeatureGrid(
    primaryActionModifier: Modifier,
    metricModifier: Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        FeatureCard(
            title = "Custom arrows",
            body = "Solid, dashed, one-sided, or both-sided paths can be configured globally or per step.",
            modifier = primaryActionModifier.weight(1f),
            color = Color(0xFFFFE0B8),
        )
        FeatureCard(
            title = "Highlight styles",
            body = "Rounded and circular targets support glow strokes, borders, and cutout control.",
            modifier = metricModifier
                .weight(1f)
                .aspectRatio(1f),
            color = Color(0xFFDDF1FF),
            circular = true,
        )
    }
}

@Composable
private fun FeatureCard(
    title: String,
    body: String,
    modifier: Modifier,
    color: Color,
    circular: Boolean = false,
) {
    val contentModifier = if (circular) {
        Modifier
            .fillMaxSize()
            .padding(22.dp)
    } else {
        Modifier.padding(18.dp)
    }

    Surface(
        modifier = modifier,
        shape = if (circular) CircleShape else RoundedCornerShape(28.dp),
        color = color,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = contentModifier,
            verticalArrangement = if (circular) Arrangement.Center else Arrangement.spacedBy(12.dp),
            horizontalAlignment = if (circular) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF173B33)),
            )
            Text(
                text = title,
                color = Color(0xFF173B33),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = if (circular) TextAlign.Center else TextAlign.Start,
            )
            Text(
                text = body,
                color = Color(0xFF51483B),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = if (circular) TextAlign.Center else TextAlign.Start,
            )
        }
    }
}

@Composable
private fun HelpCard(modifier: Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(HelpCardCornerRadius),
        color = Color(0xFF24374D),
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFB9F18C)),
                contentAlignment = Alignment.Center,
            ) {
                Text("?", color = Color(0xFF173B33), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Deep content target",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "This card starts lower in the page so the sample demonstrates GuideKit auto-scroll.",
                    color = Color(0xFFD7E4F6),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun StatusPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            color = Color(0xFF173B33),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            color = Color(0xFF776D60),
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

private fun guideSteps(targetBounds: Map<String, Rect>): List<GuideKitStep> = listOf(
    GuideKitStep(
        targetBounds = targetBounds[TargetHero],
        title = "Start with one composable",
        description = "GuideKit draws a coachmark overlay above your existing Compose UI.",
        descriptionHighlight = "coachmark overlay",
        targetHighlight = sampleRoundedTargetHighlight(HeroCardCornerRadius),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomCenter,
            lineStyle = GuideKitArrowLineStyle.Dashed,
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[TargetProgress],
        title = "Let the library own step state",
        description = "The host provides steps and callbacks while GuideKit advances, skips, and finishes internally.",
        descriptionHighlights = listOf("steps", "callbacks", "internally"),
        targetHighlight = GuideKitTargetHighlightStyle(
            shape = GuideKitTargetHighlightShape.RoundedRect,
            cornerRadius = 34.dp,
            paddingPx = 12f,
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[TargetPrimaryAction],
        title = "Override arrows per step",
        description = "Use solid lines, dashed lines, custom strokes, and arrow heads on either side.",
        descriptionHighlights = listOf("solid", "dashed", "arrow heads"),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopRight,
            to = GuideKitAnchor.CenterRight,
            lineStyle = GuideKitArrowLineStyle.Solid,
            arrowHead = GuideKitArrowHead.BothSides,
            strokes = listOf(
                GuideKitArrowStroke(widthPx = 10f, color = Color.Black.copy(alpha = 0.20f)),
                GuideKitArrowStroke(widthPx = 5f, color = Color(0xFFFFC857)),
            ),
        ),
        instructionBox = GuideKitInstructionBoxStyle(
            alignment = Alignment.BottomCenter,
            maxWidth = 420.dp,
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[TargetMetric],
        title = "Switch highlight shape",
        description = "Circular highlights are useful for icons, avatars, floating buttons, and compact controls.",
        descriptionHighlights = listOf("Circular highlights", "compact controls"),
        targetHighlight = GuideKitTargetHighlightStyle(
            shape = GuideKitTargetHighlightShape.Circle,
            paddingPx = 18f,
            glowStrokes = listOf(
                GuideKitTargetHighlightStroke(widthPx = 36f, alpha = 0.10f, color = Color(0xFF4DA3FF)),
                GuideKitTargetHighlightStroke(widthPx = 18f, alpha = 0.28f, color = Color(0xFF4DA3FF)),
                GuideKitTargetHighlightStroke(widthPx = 7f, alpha = 0.58f, color = Color(0xFF4DA3FF)),
            ),
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[TargetHelp],
        title = "Auto-scroll to hidden targets",
        description = "When content is scrollable, pass onScrollBy and GuideKit keeps the target clear of the instruction box.",
        descriptionHighlights = listOf("onScrollBy", "target clear"),
        primaryButtonText = "Finish",
        autoScroll = GuideKitAutoScrollConfig(enabled = true),
        targetHighlight = sampleRoundedTargetHighlight(HelpCardCornerRadius),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomLeft,
            curveSeed = 5,
            arrowHead = GuideKitArrowHead.TargetSide,
        ),
    ),
)

private fun sampleGuideStyle() = GuideKitStyle(
    accentColor = Color(0xFFFFC857),
    overlayColor = Color(0xFF091E1A).copy(alpha = 0.70f),
    primaryButtonContentColor = Color(0xFF173B33),
    arrowConfig = GuideKitArrowConfig(
        minVisibleDistance = 28.dp,
        dashIntervalsPx = floatArrayOf(18f, 12f),
        strokeCap = StrokeCap.Round,
    ),
    targetHighlight = sampleRoundedTargetHighlight(30.dp),
    instructionBox = GuideKitInstructionBoxStyle(
        outerPadding = PaddingValues(horizontal = 18.dp, vertical = 28.dp),
        shape = RoundedCornerShape(30.dp),
        containerColor = Color(0xFFFFF8EC),
        border = BorderStroke(1.dp, Color(0xFFFFC857).copy(alpha = 0.45f)),
        shadow = GuideKitInstructionBoxShadow(
            elevation = 32.dp,
            ambientColor = Color.Black.copy(alpha = 0.34f),
            spotColor = Color.Black.copy(alpha = 0.34f),
        ),
    ),
)

private fun sampleRoundedTargetHighlight(cornerRadius: Dp) =
    GuideKitTargetHighlightStyle(
        cornerRadius = cornerRadius,
        paddingPx = 10f,
        borderWidthPx = 3f,
    )

private fun sampleBackground() = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF5EFE3),
        Color(0xFFEAF3E5),
        Color(0xFFD9E8F4),
    ),
)

private fun Modifier.trackGuideTarget(
    key: String,
    targetBounds: MutableMap<String, Rect>,
): Modifier = onGloballyPositioned { coordinates: LayoutCoordinates ->
    targetBounds[key] = coordinates.boundsInRoot()
}
