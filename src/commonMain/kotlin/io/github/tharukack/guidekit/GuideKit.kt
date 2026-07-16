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
    val lineStyle: GuideKitArrowLineStyle = GuideKitArrowLineStyle.Dashed,
    val dashIntervalsPx: FloatArray = floatArrayOf(20f, 15f),
    val dashPhasePx: Float = 0f,
    val strokes: List<GuideKitArrowStroke> = listOf(
        GuideKitArrowStroke(widthPx = 9f, color = Color.Black.copy(alpha = 0.24f)),
        GuideKitArrowStroke(widthPx = 5.5f, color = null, alpha = 0.95f),
        GuideKitArrowStroke(widthPx = 1.7f, color = Color.White.copy(alpha = 0.62f)),
    ),
    val strokeCap: StrokeCap = StrokeCap.Round,
    val arrowHead: GuideKitArrowHead = GuideKitArrowHead.TargetSide,
    val arrowHeadLengthPx: Float = 38f,
    val arrowHeadAngleDegrees: Float = 30f,
    val arrowHeadStrokes: List<GuideKitArrowStroke> = listOf(
        GuideKitArrowStroke(widthPx = 9f, color = Color.Black.copy(alpha = 0.22f)),
        GuideKitArrowStroke(widthPx = 5.5f, color = null, alpha = 0.96f),
        GuideKitArrowStroke(widthPx = 1.6f, color = Color.White.copy(alpha = 0.55f)),
    ),
)

enum class GuideKitArrowLineStyle {
    Solid,
    Dashed,
}

enum class GuideKitArrowHead {
    None,
    TargetSide,
    InstructionBoxSide,
    BothSides,
}

data class GuideKitArrowStroke(
    val widthPx: Float,
    val color: Color? = null,
    val alpha: Float = 1f,
)

data class GuideKitStep(
    val targetBounds: Rect?,
    val title: String,
    val description: String,
    val primaryButtonText: String? = null,
    val descriptionHighlight: String? = null,
    val descriptionHighlights: List<String> = emptyList(),
    val instructionBottomPadding: Dp = 104.dp,
    val arrowConfig: GuideKitArrowConfig? = null,
    val targetHighlight: GuideKitTargetHighlightStyle? = null,
    val instructionBox: GuideKitInstructionBoxStyle? = null,
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
    val paddingPx: Float = 10f,
    val cornerRadius: Dp = 28.dp,
    val glowStrokes: List<GuideKitTargetHighlightStroke> = listOf(
        GuideKitTargetHighlightStroke(widthPx = 30f, alpha = 0.11f),
        GuideKitTargetHighlightStroke(widthPx = 22f, alpha = 0.18f),
        GuideKitTargetHighlightStroke(widthPx = 14f, alpha = 0.30f),
        GuideKitTargetHighlightStroke(widthPx = 8f, alpha = 0.45f),
    ),
    val borderColor: Color? = null,
    val borderWidthPx: Float = 2.5f,
    val innerBorderColor: Color = Color.White.copy(alpha = 0.7f),
    val innerBorderWidthPx: Float = 1.2f,
    val innerBorderInsetPx: Float = 2f,
)

enum class GuideKitTargetHighlightShape {
    RoundedRect,
    Circle,
}

data class GuideKitTargetHighlightStroke(
    val widthPx: Float,
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
        currentStep.descriptionHighlight,
        currentStep.descriptionHighlights,
    ) {
        buildAnnotatedString {
            val highlights = if (currentStep.descriptionHighlights.isNotEmpty()) {
                currentStep.descriptionHighlights
            } else {
                listOfNotNull(currentStep.descriptionHighlight)
            }
            val highlightRanges = highlights
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
                    val strokeStyle = Stroke(width = stroke.widthPx)
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
                if (targetHighlightStyle.borderWidthPx > 0f) {
                    val borderStyle = Stroke(width = targetHighlightStyle.borderWidthPx)
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
                if (targetHighlightStyle.innerBorderWidthPx > 0f) {
                    val inset = targetHighlightStyle.innerBorderInsetPx.coerceAtLeast(0f)
                    val innerBorderStyle = Stroke(width = targetHighlightStyle.innerBorderWidthPx)
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
    val pathEffect = when (config.lineStyle) {
        GuideKitArrowLineStyle.Solid -> null
        GuideKitArrowLineStyle.Dashed -> config.dashIntervalsPx
            .takeIf { it.isNotEmpty() }
            ?.let { intervals ->
                PathEffect.dashPathEffect(
                    intervals = intervals,
                    phase = config.dashPhasePx,
                )
            }
    }
    val path = Path().apply {
        moveTo(start.x, start.y)
        cubicTo(
            firstControl.x,
            firstControl.y,
            secondControl.x,
            secondControl.y,
            end.x,
            end.y,
        )
    }

    config.strokes.forEach { stroke ->
        drawPath(
            path = path,
            color = stroke.resolvedColor(accentColor = color),
            style = Stroke(
                width = stroke.widthPx,
                cap = config.strokeCap,
                pathEffect = pathEffect,
            ),
        )
    }

    if (config.arrowHead == GuideKitArrowHead.TargetSide || config.arrowHead == GuideKitArrowHead.BothSides) {
        drawArrowHead(
            tip = end,
            previousPoint = secondControl,
            accentColor = color,
            config = config,
        )
    }
    if (config.arrowHead == GuideKitArrowHead.InstructionBoxSide || config.arrowHead == GuideKitArrowHead.BothSides) {
        drawArrowHead(
            tip = start,
            previousPoint = firstControl,
            accentColor = color,
            config = config,
        )
    }
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
    val wingLength = config.arrowHeadLengthPx
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
                    width = stroke.widthPx,
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

private fun Float.toRadians(): Float = (this * PI / 180f).toFloat()
