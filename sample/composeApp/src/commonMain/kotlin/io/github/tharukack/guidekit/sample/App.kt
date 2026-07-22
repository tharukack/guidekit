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
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
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

private const val StudioHero = "studioHero"
private const val StudioTimeline = "studioTimeline"
private const val StudioAudience = "studioAudience"
private const val StudioLaunch = "studioLaunch"
private const val StudioSupport = "studioSupport"

private val HeroCardCornerRadius = 34.dp
private val HelpCardCornerRadius = 32.dp
private val StudioHeroCornerRadius = 38.dp
private val StudioSupportCornerRadius = 30.dp

private enum class SampleDestination(
    val label: String,
    val marker: String,
) {
    Essentials("Essentials", "1"),
    Studio("Studio", "2"),
}

@Composable
fun App() {
    MaterialTheme {
        GuideKitSampleScreen()
    }
}

@Composable
private fun GuideKitSampleScreen() {
    var selectedDestination by remember { mutableStateOf(SampleDestination.Essentials) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(sampleBackground(selectedDestination)),
    ) {
        when (selectedDestination) {
            SampleDestination.Essentials -> EssentialsGuideDemo(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 92.dp),
            )
            SampleDestination.Studio -> StudioGuideDemo(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 92.dp),
            )
        }

        SampleBottomNavigation(
            selectedDestination = selectedDestination,
            onDestinationSelected = { selectedDestination = it },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun SampleBottomNavigation(
    selectedDestination: SampleDestination,
    onDestinationSelected: (SampleDestination) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = Color(0xFFFFF8EC),
        tonalElevation = 18.dp,
    ) {
        SampleDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = selectedDestination == destination,
                onClick = { onDestinationSelected(destination) },
                icon = {
                    Text(
                        text = destination.marker,
                        fontWeight = FontWeight.Black,
                    )
                },
                label = { Text(destination.label) },
            )
        }
    }
}

@Composable
private fun EssentialsGuideDemo(modifier: Modifier = Modifier) {
    val targetBounds = remember { mutableStateMapOf<String, Rect>() }
    val scrollState = rememberScrollState()
    var showTour by remember { mutableStateOf(true) }
    var lastEvent by remember { mutableStateOf("Tour is ready") }

    Box(modifier = modifier) {
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
private fun StudioGuideDemo(modifier: Modifier = Modifier) {
    val targetBounds = remember { mutableStateMapOf<String, Rect>() }
    val scrollState = rememberScrollState()
    var showTour by remember { mutableStateOf(true) }
    var lastEvent by remember { mutableStateOf("Studio tour is ready") }

    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            StudioHeroCard(
                modifier = Modifier.trackGuideTarget(StudioHero, targetBounds),
                onRestartTour = {
                    lastEvent = "Studio tour restarted"
                    showTour = true
                },
            )
            StudioTimelineCard(
                modifier = Modifier.trackGuideTarget(StudioTimeline, targetBounds),
            )
            StudioMetricsRow(
                audienceModifier = Modifier.trackGuideTarget(StudioAudience, targetBounds),
                launchModifier = Modifier.trackGuideTarget(StudioLaunch, targetBounds),
            )
            StudioSupportCard(
                modifier = Modifier.trackGuideTarget(StudioSupport, targetBounds),
            )
            Spacer(Modifier.height(240.dp))
            Text(
                text = lastEvent,
                color = Color(0xFF6A4B40),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
        }

        if (showTour) {
            GuideKit(
                steps = studioGuideSteps(targetBounds),
                style = studioGuideStyle(),
                onScrollBy = { deltaPx -> scrollState.animateScrollBy(deltaPx) },
                onStepChanged = { index -> lastEvent = "Viewing studio step ${index + 1}" },
                onSkipped = {
                    showTour = false
                    lastEvent = "Studio tour skipped"
                },
                onFinished = {
                    showTour = false
                    lastEvent = "Studio tour finished"
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
private fun StudioHeroCard(
    modifier: Modifier,
    onRestartTour: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(StudioHeroCornerRadius),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF402015)),
    ) {
        Column(
            modifier = Modifier.padding(26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text(
                text = "Launch Studio",
                color = Color(0xFFFFF1E8),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "A second tour with coral accents, different card styles, circular callouts, and alternate arrow placement.",
                color = Color(0xFFFFD7C7),
                style = MaterialTheme.typography.bodyLarge,
            )
            Button(
                onClick = onRestartTour,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B4A),
                    contentColor = Color(0xFF2D130D),
                ),
            ) {
                Text("Replay studio tour")
            }
        }
    }
}

@Composable
private fun StudioTimelineCard(modifier: Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color(0xFFFFF1E8),
        border = BorderStroke(1.dp, Color(0xFFFFB39E)),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "Campaign timeline",
                color = Color(0xFF402015),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StudioStagePill("Plan", true)
                StudioStagePill("Design", true)
                StudioStagePill("Ship", false)
            }
            Text(
                text = "Use per-step instruction cards to point at wider layout sections without changing your production UI.",
                color = Color(0xFF6A4B40),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun StudioStagePill(label: String, active: Boolean) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (active) Color(0xFFFF6B4A) else Color(0xFFFFD8CB),
    ) {
        Text(
            text = label,
            color = if (active) Color(0xFF2D130D) else Color(0xFF6A4B40),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
        )
    }
}

@Composable
private fun StudioMetricsRow(
    audienceModifier: Modifier,
    launchModifier: Modifier,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        StudioMetricCard(
            title = "Audience",
            value = "24k",
            body = "Targeted segment ready for onboarding.",
            modifier = audienceModifier.weight(1f),
            color = Color(0xFFFFD8CB),
        )
        StudioMetricCard(
            title = "Launch",
            value = "84%",
            body = "Use a circular highlight on compact progress cards.",
            modifier = launchModifier
                .weight(1f)
                .aspectRatio(1f),
            color = Color(0xFFFFB39E),
            circular = true,
        )
    }
}

@Composable
private fun StudioMetricCard(
    title: String,
    value: String,
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
        shape = if (circular) CircleShape else RoundedCornerShape(26.dp),
        color = color,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = contentModifier,
            verticalArrangement = if (circular) Arrangement.Center else Arrangement.spacedBy(8.dp),
            horizontalAlignment = if (circular) Alignment.CenterHorizontally else Alignment.Start,
        ) {
            Text(
                text = value,
                color = Color(0xFF402015),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                textAlign = if (circular) TextAlign.Center else TextAlign.Start,
            )
            Text(
                text = title,
                color = Color(0xFF402015),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = if (circular) TextAlign.Center else TextAlign.Start,
            )
            Text(
                text = body,
                color = Color(0xFF6A4B40),
                style = MaterialTheme.typography.bodySmall,
                textAlign = if (circular) TextAlign.Center else TextAlign.Start,
            )
        }
    }
}

@Composable
private fun StudioSupportCard(modifier: Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(StudioSupportCornerRadius),
        color = Color(0xFF1E2E36),
    ) {
        Row(
            modifier = Modifier.padding(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF6B4A)),
                contentAlignment = Alignment.Center,
            ) {
                Text("!", color = Color(0xFF2D130D), fontWeight = FontWeight.Black)
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Review before launch",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "This lower card demonstrates auto-scroll with a different style and accent color.",
                    color = Color(0xFFCFE1E8),
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
        descriptionHighlights = listOf("coachmark overlay"),
        targetHighlight = sampleRoundedTargetHighlight(HeroCardCornerRadius),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomCenter,
            lineStyle = GuideKitArrowLineStyle.SpacedDash,
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
            padding = 12,
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
                GuideKitArrowStroke(width = 10, color = Color.Black.copy(alpha = 0.20f)),
                GuideKitArrowStroke(width = 5, color = Color(0xFFFFC857)),
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
            padding = 18,
            glowStrokes = listOf(
                GuideKitTargetHighlightStroke(width = 36, alpha = 0.10f, color = Color(0xFF4DA3FF)),
                GuideKitTargetHighlightStroke(width = 18, alpha = 0.28f, color = Color(0xFF4DA3FF)),
                GuideKitTargetHighlightStroke(width = 7, alpha = 0.58f, color = Color(0xFF4DA3FF)),
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

private fun studioGuideSteps(targetBounds: Map<String, Rect>): List<GuideKitStep> = listOf(
    GuideKitStep(
        targetBounds = targetBounds[StudioHero],
        title = "A second visual system",
        description = "This screen reuses GuideKit with a coral accent, new shapes, and independent tour state.",
        descriptionHighlights = listOf("coral accent", "independent"),
        targetHighlight = studioRoundedTargetHighlight(StudioHeroCornerRadius),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomCenter,
            lineStyle = GuideKitArrowLineStyle.Solid,
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[StudioTimeline],
        title = "Point at wide sections",
        description = "Instruction cards can be narrower than the target while the highlight follows the full section.",
        descriptionHighlights = listOf("highlight follows the full section"),
        targetHighlight = studioRoundedTargetHighlight(30.dp),
        arrowConfig = GuideKitArrowConfig(
            enabled = false
        ),
        instructionBox = GuideKitInstructionBoxStyle(
            alignment = Alignment.BottomCenter,
            maxWidth = 230.dp,
            shape = RoundedCornerShape(28.dp),
            containerColor = Color(0xFFFFF1E8),
            border = BorderStroke(1.dp, Color(0xFFFF6B4A).copy(alpha = 0.45f)),
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[StudioAudience],
        title = "Use another arrow path",
        description = "Change anchors per step to route the arrow around different layouts.",
        descriptionHighlights = listOf("anchors", "per step"),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopRight,
            to = GuideKitAnchor.TopLeft,
            lineStyle = GuideKitArrowLineStyle.ShortDash,
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[StudioLaunch],
        title = "Circular cards work too",
        description = "Square target bounds can become clean circular highlights with one style flag.",
        descriptionHighlights = listOf("circular highlights", "one style flag"),
        arrowConfig = GuideKitArrowConfig(
            to = GuideKitAnchor.CenterLeft
        ),
        targetHighlight = GuideKitTargetHighlightStyle(
            shape = GuideKitTargetHighlightShape.Circle,
            padding = 16,
            glowStrokes = listOf(
                GuideKitTargetHighlightStroke(width = 34, alpha = 0.12f, color = Color(0xFFFF6B4A)),
                GuideKitTargetHighlightStroke(width = 18, alpha = 0.30f, color = Color(0xFFFF6B4A)),
                GuideKitTargetHighlightStroke(width = 7, alpha = 0.60f, color = Color(0xFFFF6B4A)),
            ),
        ),
    ),
    GuideKitStep(
        targetBounds = targetBounds[StudioSupport],
        title = "Auto-scroll with another theme",
        description = "The same scroll callback works across screens and styles.",
        descriptionHighlights = listOf("scroll callback", "screens and styles"),
        primaryButtonText = "Done",
        autoScroll = GuideKitAutoScrollConfig(enabled = true),
        targetHighlight = studioRoundedTargetHighlight(StudioSupportCornerRadius),
        arrowConfig = GuideKitArrowConfig(
            from = GuideKitAnchor.TopCenter,
            to = GuideKitAnchor.BottomRight,
            curveSeed = 3,
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
        lineStyle = GuideKitArrowLineStyle.MediumDash,
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

private fun studioGuideStyle() = GuideKitStyle(
    accentColor = Color(0xFFFF6B4A),
    overlayColor = Color(0xFF2D130D).copy(alpha = 0.70f),
    primaryButtonContentColor = Color(0xFF2D130D),
    arrowConfig = GuideKitArrowConfig(
        minVisibleDistance = 26.dp,
        lineStyle = GuideKitArrowLineStyle.ShortDash,
        strokeCap = StrokeCap.Round,
        strokes = listOf(
            GuideKitArrowStroke(width = 9, color = Color.Black.copy(alpha = 0.22f)),
            GuideKitArrowStroke(width = 5, color = Color(0xFFFF6B4A)),
            GuideKitArrowStroke(width = 2, color = Color.White.copy(alpha = 0.54f)),
        ),
    ),
    targetHighlight = studioRoundedTargetHighlight(28.dp),
    instructionBox = GuideKitInstructionBoxStyle(
        outerPadding = PaddingValues(horizontal = 18.dp, vertical = 28.dp),
        shape = RoundedCornerShape(28.dp),
        containerColor = Color(0xFFFFF1E8),
        contentColor = Color(0xFF402015),
        border = BorderStroke(1.dp, Color(0xFFFF6B4A).copy(alpha = 0.42f)),
        shadow = GuideKitInstructionBoxShadow(
            elevation = 34.dp,
            ambientColor = Color.Black.copy(alpha = 0.36f),
            spotColor = Color.Black.copy(alpha = 0.36f),
        ),
    ),
)

private fun sampleRoundedTargetHighlight(cornerRadius: Dp) =
    GuideKitTargetHighlightStyle(
        cornerRadius = cornerRadius,
        padding = 10,
        borderWidth = 3,
    )

private fun studioRoundedTargetHighlight(cornerRadius: Dp) =
    GuideKitTargetHighlightStyle(
        cornerRadius = cornerRadius,
        padding = 11,
        borderWidth = 3,
        borderColor = Color(0xFFFF6B4A),
        innerBorderColor = Color.White.copy(alpha = 0.74f),
    )

private fun sampleBackground(destination: SampleDestination) = when (destination) {
    SampleDestination.Essentials -> Brush.verticalGradient(
        colors = listOf(
            Color(0xFFF5EFE3),
            Color(0xFFEAF3E5),
            Color(0xFFD9E8F4),
        ),
    )
    SampleDestination.Studio -> Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF2E8),
            Color(0xFFFFE1D4),
            Color(0xFFEAF2F5),
        ),
    )
}

private fun Modifier.trackGuideTarget(
    key: String,
    targetBounds: MutableMap<String, Rect>,
): Modifier = onGloballyPositioned { coordinates: LayoutCoordinates ->
    targetBounds[key] = coordinates.boundsInRoot()
}
