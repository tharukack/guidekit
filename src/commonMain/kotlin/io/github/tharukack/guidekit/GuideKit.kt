package io.github.tharukack.guidekit

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class GuideKitStepContent(
    val stepIndex: Int,
    val title: String,
    val description: String,
    val primaryButtonText: String,
)

data class GuideKitArrowConfig(
    val enabled: Boolean = true,
    val from: GuideKitAnchor = GuideKitAnchor.TopCenter,
    val to: GuideKitAnchor = GuideKitAnchor.BottomCenter,
    val curveSeed: Int = 0,
    val minVisibleDistance: Dp = 40.dp,
    val lineStyle: GuideKitArrowLineStyle = GuideKitArrowLineStyle.SpacedDash,
    val strokes: List<GuideKitArrowStroke> = listOf(
        GuideKitArrowStroke(width = 9, color = Color.Black.copy(alpha = 0.24f)),
        GuideKitArrowStroke(width = 6, color = null, alpha = 0.95f),
        GuideKitArrowStroke(width = 2, color = Color.White.copy(alpha = 0.62f)),
    ),
    val strokeCap: StrokeCap = StrokeCap.Round,
    val arrowHead: GuideKitArrowHead = GuideKitArrowHead.TargetSide,
    val arrowHeadLength: Int = 38,
    val arrowHeadAngleDegrees: Int = 30,
    val arrowHeadStrokes: List<GuideKitArrowStroke> = listOf(
        GuideKitArrowStroke(width = 9, color = Color.Black.copy(alpha = 0.22f)),
        GuideKitArrowStroke(width = 6, color = null, alpha = 0.96f),
        GuideKitArrowStroke(width = 2, color = Color.White.copy(alpha = 0.55f)),
    ),
)

enum class GuideKitArrowLineStyle {
    Solid,
    SpacedDash,
    Dotted,
    ShortDash,
    MediumDash,
    LongDash,
    DashDot,
}

enum class GuideKitArrowHead {
    None,
    TargetSide,
    InstructionBoxSide,
    BothSides,
}

data class GuideKitArrowStroke(
    val width: Int,
    val color: Color? = null,
    val alpha: Float = 1f,
)

data class GuideKitStep(
    val targetBounds: Rect?,
    val title: String,
    val description: String,
    val primaryButtonText: String? = null,
    val descriptionHighlights: List<String> = emptyList(),
    val instructionBottomPadding: Dp = 104.dp,
    val arrowConfigOverride: GuideKitArrowConfigOverride? = null,
    val targetHighlightOverride: GuideKitTargetHighlightStyleOverride? = null,
    val instructionBoxOverride: GuideKitInstructionBoxStyleOverride? = null,
    val autoScroll: GuideKitAutoScrollConfig = GuideKitAutoScrollConfig(),
)

data class GuideKitAutoScrollConfig(
    val enabled: Boolean = true,
    val minTopVisibleDistance: Dp? = null,
    val spacing: Dp? = null,
)

data class GuideKitStyle(
    val accentColor: Color = Color(0xFF5ED5B3),
    val overlayColor: Color = Color.Black.copy(alpha = 0.68f),
    val titleColor: Color? = null,
    val descriptionColor: Color? = null,
    val highlightedDescriptionColor: Color? = null,
    val stepIndicatorActiveColor: Color? = null,
    val stepIndicatorInactiveColor: Color? = null,
    val primaryButtonContainerColor: Color? = null,
    val primaryButtonContentColor: Color = Color(0xFF062D25),
    val skipIconTint: Color? = null,
    val arrowConfig: GuideKitArrowConfig = GuideKitArrowConfig(),
    val targetHighlight: GuideKitTargetHighlightStyle = GuideKitTargetHighlightStyle(),
    val instructionBox: GuideKitInstructionBoxStyle = GuideKitInstructionBoxStyle(),
)

data class GuideKitTargetHighlightStyle(
    val enabled: Boolean = true,
    val shape: GuideKitTargetHighlightShape = GuideKitTargetHighlightShape.RoundedRect,
    val cutoutEnabled: Boolean = true,
    val padding: Int = 10,
    val cornerRadius: Dp = 28.dp,
    val glowStrokes: List<GuideKitTargetHighlightStroke> = listOf(
        GuideKitTargetHighlightStroke(width = 30, alpha = 0.11f),
        GuideKitTargetHighlightStroke(width = 22, alpha = 0.18f),
        GuideKitTargetHighlightStroke(width = 14, alpha = 0.30f),
        GuideKitTargetHighlightStroke(width = 8, alpha = 0.45f),
    ),
    val borderColor: Color? = null,
    val borderWidth: Int = 3,
    val innerBorderColor: Color = Color.White.copy(alpha = 0.7f),
    val innerBorderWidth: Int = 1,
    val innerBorderInset: Int = 2,
)

enum class GuideKitTargetHighlightShape {
    RoundedRect,
    Circle,
}

data class GuideKitTargetHighlightStroke(
    val width: Int,
    val alpha: Float,
    val color: Color? = null,
)

data class GuideKitInstructionBoxStyle(
    val alignment: Alignment = Alignment.BottomCenter,
    val outerPadding: PaddingValues? = null,
    val contentPadding: PaddingValues = PaddingValues(horizontal = 22.dp, vertical = 24.dp),
    val fillMaxWidth: Boolean = true,
    val minWidth: Dp? = null,
    val maxWidth: Dp? = null,
    val minHeight: Dp? = null,
    val maxHeight: Dp? = null,
    val shape: Shape = RoundedCornerShape(30.dp),
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val border: BorderStroke? = BorderStroke(1.dp, Color(0xFF5ED5B3).copy(alpha = 0.28f)),
    val tonalElevation: Dp = 0.dp,
    val shadowElevation: Dp = 26.dp,
    val modifier: Modifier = Modifier,
    val shadow: GuideKitInstructionBoxShadow? = GuideKitInstructionBoxShadow(),
)

data class GuideKitInstructionBoxShadow(
    val elevation: Dp = 30.dp,
    val ambientColor: Color = Color.Black.copy(alpha = 0.42f),
    val spotColor: Color = Color.Black.copy(alpha = 0.42f),
)

sealed interface GuideKitOverride<out T> {
    data object Inherit : GuideKitOverride<Nothing>

    data class Value<T>(val value: T) : GuideKitOverride<T>
}

data class GuideKitArrowConfigOverride(
    val enabled: Boolean? = null,
    val from: GuideKitAnchor? = null,
    val to: GuideKitAnchor? = null,
    val curveSeed: Int? = null,
    val minVisibleDistance: Dp? = null,
    val lineStyle: GuideKitArrowLineStyle? = null,
    val strokes: List<GuideKitArrowStroke>? = null,
    val strokeCap: StrokeCap? = null,
    val arrowHead: GuideKitArrowHead? = null,
    val arrowHeadLength: Int? = null,
    val arrowHeadAngleDegrees: Int? = null,
    val arrowHeadStrokes: List<GuideKitArrowStroke>? = null,
)

data class GuideKitTargetHighlightStyleOverride(
    val enabled: Boolean? = null,
    val shape: GuideKitTargetHighlightShape? = null,
    val cutoutEnabled: Boolean? = null,
    val padding: Int? = null,
    val cornerRadius: Dp? = null,
    val glowStrokes: List<GuideKitTargetHighlightStroke>? = null,
    val borderColor: Color? = null,
    val borderColorOverride: GuideKitOverride<Color?> = GuideKitOverride.Inherit,
    val borderWidth: Int? = null,
    val innerBorderColor: Color? = null,
    val innerBorderWidth: Int? = null,
    val innerBorderInset: Int? = null,
)

data class GuideKitInstructionBoxStyleOverride(
    val alignment: Alignment? = null,
    val outerPadding: PaddingValues? = null,
    val outerPaddingOverride: GuideKitOverride<PaddingValues?> = GuideKitOverride.Inherit,
    val contentPadding: PaddingValues? = null,
    val fillMaxWidth: Boolean? = null,
    val minWidth: Dp? = null,
    val minWidthOverride: GuideKitOverride<Dp?> = GuideKitOverride.Inherit,
    val maxWidth: Dp? = null,
    val maxWidthOverride: GuideKitOverride<Dp?> = GuideKitOverride.Inherit,
    val minHeight: Dp? = null,
    val minHeightOverride: GuideKitOverride<Dp?> = GuideKitOverride.Inherit,
    val maxHeight: Dp? = null,
    val maxHeightOverride: GuideKitOverride<Dp?> = GuideKitOverride.Inherit,
    val shape: Shape? = null,
    val containerColor: Color? = null,
    val containerColorOverride: GuideKitOverride<Color?> = GuideKitOverride.Inherit,
    val contentColor: Color? = null,
    val contentColorOverride: GuideKitOverride<Color?> = GuideKitOverride.Inherit,
    val border: BorderStroke? = null,
    val borderOverride: GuideKitOverride<BorderStroke?> = GuideKitOverride.Inherit,
    val tonalElevation: Dp? = null,
    val shadowElevation: Dp? = null,
    val modifier: Modifier? = null,
    val shadow: GuideKitInstructionBoxShadow? = null,
    val shadowOverride: GuideKitOverride<GuideKitInstructionBoxShadow?> = GuideKitOverride.Inherit,
)

enum class GuideKitAnchor {
    TopLeft,
    TopCenter,
    TopRight,
    CenterLeft,
    Center,
    CenterRight,
    BottomLeft,
    BottomCenter,
    BottomRight,
}

@Composable
fun GuideKit(
    steps: List<GuideKitStep>,
    modifier: Modifier = Modifier,
    initialStepIndex: Int = 0,
    showStepIndicator: Boolean = true,
    style: GuideKitStyle = GuideKitStyle(),
    onStepChanged: (Int) -> Unit = {},
    onScrollBy: suspend (Float) -> Float = { 0f },
    onSkipped: (() -> Unit)? = null,
    onFinished: () -> Unit,
) {
    if (steps.isEmpty()) return

    var currentStepIndex by remember {
        mutableStateOf(initialStepIndex.coerceIn(0, steps.lastIndex))
    }
    LaunchedEffect(steps.size) {
        if (currentStepIndex > steps.lastIndex) {
            currentStepIndex = steps.lastIndex
        }
    }

    val safeCurrentStepIndex = currentStepIndex.coerceIn(0, steps.lastIndex)
    LaunchedEffect(safeCurrentStepIndex) {
        onStepChanged(safeCurrentStepIndex)
    }
    val resolvedStep = resolveGuideKitStep(
        steps = steps,
        requestedStepIndex = safeCurrentStepIndex,
        style = style,
    ) ?: return
    val currentStep = resolvedStep.step
    val isLastStep = resolvedStep.isLastStep
    val primaryButtonText = resolvedStep.primaryButtonText
    val onPreviousClick = if (safeCurrentStepIndex > 0) {
        {
            when (val result = GuideKitController(steps.size, safeCurrentStepIndex).previous()) {
                is GuideKitNavigationResult.StepChanged -> currentStepIndex = result.index
                else -> Unit
            }
        }
    } else {
        null
    }
    val onPrimaryClick = {
        when (val result = GuideKitController(steps.size, safeCurrentStepIndex).next()) {
            GuideKitNavigationResult.Finished -> onFinished()
            is GuideKitNavigationResult.StepChanged -> currentStepIndex = result.index
            else -> Unit
        }
    }
    val accentColor = style.accentColor
    val arrowConfig = resolvedStep.arrowConfig
    val targetHighlightStyle = resolvedStep.targetHighlight
    val instructionBoxStyle = resolvedStep.instructionBox
    val autoScrollConfig = resolvedStep.autoScroll
    val density = LocalDensity.current
    val instructionBoxShape = instructionBoxStyle.shape
    val instructionBoxOuterPadding = instructionBoxStyle.outerPadding
        ?: PaddingValues(start = 18.dp, end = 18.dp, bottom = currentStep.instructionBottomPadding)
    val instructionBoxShadow = instructionBoxStyle.shadow
    val highlightBounds = resolvedStep.highlightBounds
    var instructionBounds by remember { mutableStateOf<Rect?>(null) }
    var previousSwipeOffset by remember { mutableStateOf(0f) }
    val stepContent = GuideKitStepContent(
        stepIndex = safeCurrentStepIndex,
        title = currentStep.title,
        description = currentStep.description,
        primaryButtonText = primaryButtonText,
    )
    val highlightedDescription = remember(
        currentStep.description,
        currentStep.descriptionHighlights,
    ) {
        buildAnnotatedString {
            val highlightRanges = currentStep.descriptionHighlights
                .mapNotNull { highlight ->
                    val start = currentStep.description.indexOf(highlight)
                    if (start >= 0) start until (start + highlight.length) else null
                }
                .sortedBy { it.first }
            if (highlightRanges.isEmpty()) {
                append(currentStep.description)
            } else {
                var currentIndex = 0
                highlightRanges.forEach { range ->
                    if (range.first > currentIndex) {
                        append(currentStep.description.substring(currentIndex, range.first))
                    }
                    withStyle(
                        SpanStyle(
                            color = style.highlightedDescriptionColor ?: accentColor,
                            fontWeight = FontWeight.Bold,
                        ),
                    ) {
                        append(currentStep.description.substring(range.first, range.last + 1))
                    }
                    currentIndex = range.last + 1
                }
                if (currentIndex < currentStep.description.length) {
                    append(currentStep.description.substring(currentIndex))
                }
            }
        }
    }
    LaunchedEffect(
        safeCurrentStepIndex,
        highlightBounds,
        instructionBounds,
        autoScrollConfig,
        arrowConfig.minVisibleDistance,
    ) {
        val target = highlightBounds ?: return@LaunchedEffect
        val instruction = instructionBounds ?: return@LaunchedEffect
        if (!autoScrollConfig.enabled) return@LaunchedEffect

        val minTopVisibleDistancePx = with(density) {
            (autoScrollConfig.minTopVisibleDistance ?: (arrowConfig.minVisibleDistance + 1.dp)).toPx()
        }
        val spacingPx = with(density) {
            (autoScrollConfig.spacing ?: (arrowConfig.minVisibleDistance + 1.dp)).toPx()
        }
        val requestedScrollPx = calculateGuideKitAutoScrollDelta(
            targetBounds = target,
            instructionBounds = instruction,
            enabled = autoScrollConfig.enabled,
            spacingPx = spacingPx,
            minTopVisibleDistancePx = minTopVisibleDistancePx,
        )
        if (requestedScrollPx != 0f) {
            onScrollBy(requestedScrollPx)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        /*
        Step 1 fallback was a plain dark overlay:
        Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.68f))

        Current step uses the measured target bounds to cut out the Daily card area.
        */
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    compositingStrategy = CompositingStrategy.Offscreen
                },
        ) {
            drawRect(style.overlayColor)
            if (highlightBounds != null) {
                val topLeft = Offset(highlightBounds.left, highlightBounds.top)
                val size = Size(highlightBounds.width, highlightBounds.height)
                val highlightRadiusPx = targetHighlightStyle.cornerRadius.toPx()
                val cornerRadius = CornerRadius(highlightRadiusPx, highlightRadiusPx)
                val circleRadius = highlightBounds.width.coerceAtLeast(highlightBounds.height) / 2f
                if (targetHighlightStyle.cutoutEnabled) {
                    when (targetHighlightStyle.shape) {
                        GuideKitTargetHighlightShape.RoundedRect -> {
                            drawRoundRect(
                                color = Color.Transparent,
                                topLeft = topLeft,
                                size = size,
                                cornerRadius = cornerRadius,
                                blendMode = BlendMode.Clear,
                            )
                        }

                        GuideKitTargetHighlightShape.Circle -> {
                            drawCircle(
                                color = Color.Transparent,
                                radius = circleRadius,
                                center = highlightBounds.center,
                                blendMode = BlendMode.Clear,
                            )
                        }
                    }
                }
                targetHighlightStyle.glowStrokes.forEach { stroke ->
                    val strokeStyle = Stroke(width = stroke.width.toGuideKitPx())
                    val strokeColor = (stroke.color ?: accentColor).copy(alpha = stroke.alpha)
                    when (targetHighlightStyle.shape) {
                        GuideKitTargetHighlightShape.RoundedRect -> {
                            drawRoundRect(
                                color = strokeColor,
                                topLeft = topLeft,
                                size = size,
                                cornerRadius = cornerRadius,
                                style = strokeStyle,
                            )
                        }

                        GuideKitTargetHighlightShape.Circle -> {
                            drawCircle(
                                color = strokeColor,
                                radius = circleRadius,
                                center = highlightBounds.center,
                                style = strokeStyle,
                            )
                        }
                    }
                }
                if (targetHighlightStyle.borderWidth > 0) {
                    val borderStyle = Stroke(width = targetHighlightStyle.borderWidth.toGuideKitPx())
                    val borderColor = targetHighlightStyle.borderColor ?: accentColor
                    when (targetHighlightStyle.shape) {
                        GuideKitTargetHighlightShape.RoundedRect -> {
                            drawRoundRect(
                                color = borderColor,
                                topLeft = topLeft,
                                size = size,
                                cornerRadius = cornerRadius,
                                style = borderStyle,
                            )
                        }

                        GuideKitTargetHighlightShape.Circle -> {
                            drawCircle(
                                color = borderColor,
                                radius = circleRadius,
                                center = highlightBounds.center,
                                style = borderStyle,
                            )
                        }
                    }
                }
                if (targetHighlightStyle.innerBorderWidth > 0) {
                    val inset = targetHighlightStyle.innerBorderInset.toGuideKitPx()
                    val innerBorderStyle = Stroke(width = targetHighlightStyle.innerBorderWidth.toGuideKitPx())
                    when (targetHighlightStyle.shape) {
                        GuideKitTargetHighlightShape.RoundedRect -> {
                            drawRoundRect(
                                color = targetHighlightStyle.innerBorderColor,
                                topLeft = Offset(
                                    x = highlightBounds.left + inset,
                                    y = highlightBounds.top + inset,
                                ),
                                size = Size(
                                    width = (highlightBounds.width - (inset * 2f)).coerceAtLeast(0f),
                                    height = (highlightBounds.height - (inset * 2f)).coerceAtLeast(0f),
                                ),
                                cornerRadius = CornerRadius(
                                    x = (highlightRadiusPx - inset).coerceAtLeast(0f),
                                    y = (highlightRadiusPx - inset).coerceAtLeast(0f),
                                ),
                                style = innerBorderStyle,
                            )
                        }

                        GuideKitTargetHighlightShape.Circle -> {
                            drawCircle(
                                color = targetHighlightStyle.innerBorderColor,
                                radius = (circleRadius - inset).coerceAtLeast(0f),
                                center = highlightBounds.center,
                                style = innerBorderStyle,
                            )
                        }
                    }
                }

                val cardBounds = instructionBounds
                if (cardBounds != null && arrowConfig.enabled) {
                    drawGuideKitArrow(
                        cardBounds = cardBounds,
                        highlightBounds = highlightBounds,
                        color = accentColor,
                        config = arrowConfig,
                    )
                }
            }
        }

        /*
        Previous highlight used a composable shadow layer, but that can sit over the cutout/card:
        Box(
            modifier = Modifier
                .offset { ... }
                .size(...)
                .shadow(...)
                .border(...)
                        )
        )
        */

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
        )

        Surface(
            modifier = Modifier
                .align(instructionBoxStyle.alignment)
                .padding(instructionBoxOuterPadding)
                .then(if (instructionBoxStyle.fillMaxWidth) Modifier.fillMaxWidth() else Modifier)
                .widthIn(
                    min = instructionBoxStyle.minWidth ?: Dp.Unspecified,
                    max = instructionBoxStyle.maxWidth ?: Dp.Unspecified,
                )
                .heightIn(
                    min = instructionBoxStyle.minHeight ?: Dp.Unspecified,
                    max = instructionBoxStyle.maxHeight ?: Dp.Unspecified,
                )
                .then(instructionBoxStyle.modifier)
                .then(
                    if (instructionBoxShadow != null) {
                        Modifier.shadow(
                            elevation = instructionBoxShadow.elevation,
                            shape = instructionBoxShape,
                            ambientColor = instructionBoxShadow.ambientColor,
                            spotColor = instructionBoxShadow.spotColor,
                        )
                    } else {
                        Modifier
                    },
                )
                .onGloballyPositioned { coordinates ->
                    instructionBounds = coordinates.boundsInParent()
                },
            shape = instructionBoxShape,
            color = instructionBoxStyle.containerColor ?: MaterialTheme.colorScheme.surface,
            contentColor = instructionBoxStyle.contentColor ?: MaterialTheme.colorScheme.onSurface,
            border = instructionBoxStyle.border,
            tonalElevation = instructionBoxStyle.tonalElevation,
            shadowElevation = instructionBoxStyle.shadowElevation,
        ) {
            Box {
                Column(
                    modifier = Modifier.padding(instructionBoxStyle.contentPadding),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    AnimatedContent(
                        targetState = stepContent,
                        transitionSpec = {
                            val forward = targetState.stepIndex >= initialState.stepIndex
                            val direction = if (forward) 1 else -1
                            (
                                slideInHorizontally(
                                    animationSpec = tween(durationMillis = 260),
                                    initialOffsetX = { width -> width / 4 * direction },
                                ) + fadeIn(animationSpec = tween(durationMillis = 180))
                                ).togetherWith(
                                    slideOutHorizontally(
                                        animationSpec = tween(durationMillis = 220),
                                        targetOffsetX = { width -> -width / 5 * direction },
                                    ) + fadeOut(animationSpec = tween(durationMillis = 140)),
                                ).using(SizeTransform(clip = false))
                        },
                        label = "guidekit-step-content",
                    ) { content ->
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        alpha = 1f - ((previousSwipeOffset / 140f).coerceIn(0f, 1f) * 0.18f)
                                    }
                                    .padding(end = previousSwipeOffset.coerceAtMost(18f).dp)
                                    .then(
                                        Modifier.graphicsLayer {
                                            translationX = previousSwipeOffset
                                        },
                                    )
                                    .pointerInput(onPreviousClick) {
                                        if (onPreviousClick == null) return@pointerInput
                                        val threshold = 72.dp.toPx()
                                        var totalDrag = 0f
                                        detectHorizontalDragGestures(
                                            onDragStart = {
                                                totalDrag = 0f
                                                previousSwipeOffset = 0f
                                            },
                                            onHorizontalDrag = { change, dragAmount ->
                                                totalDrag = (totalDrag + dragAmount).coerceAtLeast(0f)
                                                previousSwipeOffset = totalDrag.coerceIn(0f, 96.dp.toPx())
                                                change.consume()
                                            },
                                            onDragEnd = {
                                                if (totalDrag > threshold) {
                                                    onPreviousClick()
                                                }
                                                previousSwipeOffset = 0f
                                            },
                                            onDragCancel = {
                                                previousSwipeOffset = 0f
                                            },
                                        )
                                    },
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    text = content.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Left,
                                    color = style.titleColor ?: MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(end = if (onSkipped != null) 42.dp else 0.dp),
                                )
                                Text(
                                    text = highlightedDescription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Left,
                                    color = style.descriptionColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (showStepIndicator) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    repeat(steps.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(width = if (index == content.stepIndex) 18.dp else 6.dp, height = 6.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (index == content.stepIndex) {
                                                        style.stepIndicatorActiveColor ?: accentColor
                                                    } else {
                                                        style.stepIndicatorInactiveColor ?: MaterialTheme.colorScheme.outlineVariant
                                                    },
                                                ),
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(0.dp))
                            Button(
                                onClick = onPrimaryClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = style.primaryButtonContainerColor ?: accentColor,
                                    contentColor = style.primaryButtonContentColor,
                                ),
                            ) {
                                Text(
                                    text = content.primaryButtonText,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                if (onSkipped != null) {
                    IconButton(
                        onClick = onSkipped,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 10.dp, end = 10.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Skip onboarding",
                            tint = style.skipIconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGuideKitArrow(
    cardBounds: Rect,
    highlightBounds: Rect,
    color: Color,
    config: GuideKitArrowConfig,
) {
    if (cardBounds.distanceTo(highlightBounds) < config.minVisibleDistance.toPx()) return

    val start = config.from.pointOn(
        bounds = cardBounds,
        outside = true,
        padding = 6.dp.toPx(),
    )
    val end = config.to.pointOn(
        bounds = highlightBounds,
        outside = true,
        padding = 6.dp.toPx(),
    )
    val distance = start.distanceTo(end)
    val controlDistance = (distance * 0.34f).coerceIn(90f, 190f)
    val startDirection = config.from.naturalDirection(start, end, isStart = true)
    val endDirection = config.to.naturalDirection(start, end, isStart = false)
    val bendDirection = config.curveBendDirection(start, end)
    val bendAmount = config.curveBendAmount(distance)
    val firstControl = start
        .plusScaled(startDirection, controlDistance)
        .plusScaled(bendDirection, bendAmount)
    val secondControl = end
        .minusScaled(endDirection, controlDistance)
        .minusScaled(bendDirection, bendAmount * 0.72f)
    val hasTargetArrowHead = config.arrowHead == GuideKitArrowHead.TargetSide ||
        config.arrowHead == GuideKitArrowHead.BothSides
    val hasInstructionArrowHead = config.arrowHead == GuideKitArrowHead.InstructionBoxSide ||
        config.arrowHead == GuideKitArrowHead.BothSides
    val targetArrowDirection = end.minus(secondControl).normalized()
    val instructionArrowDirection = start.minus(firstControl).normalized()
    val arrowHeadNeckDistance = config.arrowHeadNeckDistance(distance)
    val bodyStart = if (hasInstructionArrowHead) {
        start.minusScaled(instructionArrowDirection, arrowHeadNeckDistance)
    } else {
        start
    }
    val bodyEnd = if (hasTargetArrowHead) {
        end.minusScaled(targetArrowDirection, arrowHeadNeckDistance)
    } else {
        end
    }
    val bodyDistance = bodyStart.distanceTo(bodyEnd)
    val bodyControlDistance = (bodyDistance * 0.34f).coerceIn(70f, 180f)
    val bodyFirstControl = bodyStart
        .plusScaled(startDirection, bodyControlDistance)
        .plusScaled(bendDirection, bendAmount)
    val bodySecondControl = bodyEnd
        .minusScaled(endDirection, bodyControlDistance)
        .minusScaled(bendDirection, bendAmount * 0.72f)
    val pathEffect = config.lineStyle.dashIntervalsPx()?.let { intervals ->
        PathEffect.dashPathEffect(intervals = intervals)
    }
    val path = Path().apply {
        moveTo(bodyStart.x, bodyStart.y)
        cubicTo(
            bodyFirstControl.x,
            bodyFirstControl.y,
            bodySecondControl.x,
            bodySecondControl.y,
            bodyEnd.x,
            bodyEnd.y,
        )
    }

    config.strokes.forEach { stroke ->
        drawPath(
            path = path,
            color = stroke.resolvedColor(accentColor = color),
            style = Stroke(
                width = stroke.width.toGuideKitPx(),
                cap = config.strokeCap,
                pathEffect = pathEffect,
            ),
        )
    }

    if (hasTargetArrowHead) {
        drawArrowHead(
            tip = end,
            previousPoint = bodyEnd,
            accentColor = color,
            config = config,
        )
    }
    if (hasInstructionArrowHead) {
        drawArrowHead(
            tip = start,
            previousPoint = bodyStart,
            accentColor = color,
            config = config,
        )
    }
}

private fun GuideKitArrowLineStyle.dashIntervalsPx(): FloatArray? = when (this) {
    GuideKitArrowLineStyle.Solid -> null
    GuideKitArrowLineStyle.SpacedDash -> floatArrayOf(20f, 15f)
    GuideKitArrowLineStyle.Dotted -> floatArrayOf(1f, 10f)
    GuideKitArrowLineStyle.ShortDash -> floatArrayOf(8f, 8f)
    GuideKitArrowLineStyle.MediumDash -> floatArrayOf(16f, 12f)
    GuideKitArrowLineStyle.LongDash -> floatArrayOf(32f, 18f)
    GuideKitArrowLineStyle.DashDot -> floatArrayOf(24f, 10f, 1f, 10f)
}

private fun GuideKitAnchor.pointOn(
    bounds: Rect,
    outside: Boolean,
    padding: Float,
): Offset {
    val edgeInset = bounds.width.coerceAtMost(bounds.height) * 0.18f
    val left = bounds.left + edgeInset
    val right = bounds.right - edgeInset
    val centerLeft = if (outside) bounds.left - padding else bounds.left + padding
    val centerRight = if (outside) bounds.right + padding else bounds.right - padding
    val top = if (outside) bounds.top - padding else bounds.top + padding
    val bottom = if (outside) bounds.bottom + padding else bounds.bottom - padding
    return when (this) {
        GuideKitAnchor.TopLeft -> Offset(left, top)
        GuideKitAnchor.TopCenter -> Offset(bounds.center.x, top)
        GuideKitAnchor.TopRight -> Offset(right, top)
        GuideKitAnchor.CenterLeft -> Offset(centerLeft, bounds.center.y)
        GuideKitAnchor.Center -> bounds.center
        GuideKitAnchor.CenterRight -> Offset(centerRight, bounds.center.y)
        GuideKitAnchor.BottomLeft -> Offset(left, bottom)
        GuideKitAnchor.BottomCenter -> Offset(bounds.center.x, bottom)
        GuideKitAnchor.BottomRight -> Offset(right, bottom)
    }
}

private fun GuideKitAnchor.naturalDirection(start: Offset, end: Offset, isStart: Boolean): Offset {
    val fallback = if (isStart) end.minus(start).normalized() else end.minus(start).normalized()
    return when (this) {
        GuideKitAnchor.TopLeft,
        GuideKitAnchor.TopCenter,
        GuideKitAnchor.TopRight -> if (isStart) Offset(0f, -1f) else Offset(0f, 1f)

        GuideKitAnchor.BottomLeft,
        GuideKitAnchor.BottomCenter,
        GuideKitAnchor.BottomRight -> if (isStart) Offset(0f, 1f) else Offset(0f, -1f)

        GuideKitAnchor.CenterLeft -> if (isStart) Offset(-1f, 0f) else Offset(1f, 0f)
        GuideKitAnchor.CenterRight -> if (isStart) Offset(1f, 0f) else Offset(-1f, 0f)
        GuideKitAnchor.Center -> fallback
    }
}

private fun GuideKitArrowConfig.curveBendDirection(start: Offset, end: Offset): Offset {
    val lineDirection = end.minus(start).normalized()
    val perpendicular = Offset(-lineDirection.y, lineDirection.x)
    val hash = (from.ordinal * 31) + (to.ordinal * 17) + curveSeed
    val sign = if (hash % 2 == 0) 1f else -1f
    return perpendicular.times(sign)
}

private fun GuideKitArrowConfig.curveBendAmount(distance: Float): Float {
    val variation = (((from.ordinal * 19) + (to.ordinal * 13) + (curveSeed * 7)) % 4) * 18f
    return (distance * 0.16f + variation).coerceIn(44f, 128f)
}

private fun GuideKitArrowConfig.arrowHeadNeckDistance(distance: Float): Float {
    if (arrowHead == GuideKitArrowHead.None) return 0f

    val wingAngle = arrowHeadAngleDegrees.toRadians()
    val projectedWingLength = arrowHeadLength.toGuideKitPx() * cos(wingAngle)
    val widestLineStroke = strokes.maxOfOrNull { it.width }?.toGuideKitPx() ?: 0f
    val naturalNeckDistance = projectedWingLength + (widestLineStroke * 0.35f)
    return naturalNeckDistance.coerceAtMost(distance * 0.36f)
}

private fun Rect.distanceTo(other: Rect): Float {
    val horizontalGap = when {
        right < other.left -> other.left - right
        other.right < left -> left - other.right
        else -> 0f
    }
    val verticalGap = when {
        bottom < other.top -> other.top - bottom
        other.bottom < top -> top - other.bottom
        else -> 0f
    }
    return sqrt((horizontalGap * horizontalGap) + (verticalGap * verticalGap))
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = other.x - x
    val dy = other.y - y
    return sqrt((dx * dx) + (dy * dy))
}

private fun Offset.normalized(): Offset {
    val length = sqrt((x * x) + (y * y))
    return if (length == 0f) Offset.Zero else Offset(x / length, y / length)
}

private fun Offset.plusScaled(direction: Offset, distance: Float): Offset =
    Offset(x + direction.x * distance, y + direction.y * distance)

private fun Offset.minusScaled(direction: Offset, distance: Float): Offset =
    Offset(x - direction.x * distance, y - direction.y * distance)

private fun Offset.times(value: Float): Offset = Offset(x * value, y * value)

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrowHead(
    tip: Offset,
    previousPoint: Offset,
    accentColor: Color,
    config: GuideKitArrowConfig,
) {
    val angle = atan2(tip.y - previousPoint.y, tip.x - previousPoint.x)
    val wingAngle = config.arrowHeadAngleDegrees.toRadians()
    val wingLength = config.arrowHeadLength.toGuideKitPx()
    val left = Offset(
        x = tip.x - wingLength * cos(angle - wingAngle),
        y = tip.y - wingLength * sin(angle - wingAngle),
    )
    val right = Offset(
        x = tip.x - wingLength * cos(angle + wingAngle),
        y = tip.y - wingLength * sin(angle + wingAngle),
    )
    val leftWing = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(left.x, left.y)
    }
    val rightWing = Path().apply {
        moveTo(tip.x, tip.y)
        lineTo(right.x, right.y)
    }
    listOf(leftWing, rightWing).forEach { wing ->
        config.arrowHeadStrokes.forEach { stroke ->
            drawPath(
                path = wing,
                color = stroke.resolvedColor(accentColor = accentColor),
                style = Stroke(
                    width = stroke.width.toGuideKitPx(),
                    cap = config.strokeCap,
                ),
            )
        }
    }
}

private fun GuideKitArrowStroke.resolvedColor(accentColor: Color): Color =
    color?.let { explicitColor ->
        if (alpha == 1f) explicitColor else explicitColor.copy(alpha = alpha)
    } ?: accentColor.copy(alpha = alpha)

private fun Int.toRadians(): Float = (toFloat() * PI / 180f).toFloat()
