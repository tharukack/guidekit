package io.github.tharukack.guidekit

import androidx.compose.ui.geometry.Rect

internal fun Int.toGuideKitPx(): Float = coerceAtLeast(0).toFloat()

class GuideKitController(
    stepCount: Int,
    initialStepIndex: Int = 0,
) {
    private var stepCount: Int = stepCount.coerceAtLeast(0)

    var currentStepIndex: Int = if (hasSteps) initialStepIndex.coerceIn(0, this.stepCount - 1) else NoStepIndex
        private set

    val hasSteps: Boolean
        get() = stepCount > 0

    val isFirstStep: Boolean
        get() = hasSteps && currentStepIndex == 0

    val isLastStep: Boolean
        get() = hasSteps && currentStepIndex == stepCount - 1

    fun next(): GuideKitNavigationResult {
        if (!hasSteps) return GuideKitNavigationResult.NoSteps
        if (isLastStep) return GuideKitNavigationResult.Finished

        currentStepIndex += 1
        return GuideKitNavigationResult.StepChanged(currentStepIndex)
    }

    fun previous(): GuideKitNavigationResult {
        if (!hasSteps) return GuideKitNavigationResult.NoSteps
        if (!isFirstStep) {
            currentStepIndex -= 1
        }
        return GuideKitNavigationResult.StepChanged(currentStepIndex)
    }

    fun skip(): GuideKitNavigationResult =
        if (hasSteps) GuideKitNavigationResult.Skipped else GuideKitNavigationResult.NoSteps

    fun dismissOverlay(): GuideKitNavigationResult = skip()

    fun updateStepCount(stepCount: Int): GuideKitNavigationResult {
        this.stepCount = stepCount.coerceAtLeast(0)
        currentStepIndex = if (hasSteps) {
            currentStepIndex.coerceIn(0, this.stepCount - 1)
        } else {
            NoStepIndex
        }
        return if (hasSteps) GuideKitNavigationResult.StepChanged(currentStepIndex) else GuideKitNavigationResult.NoSteps
    }

    companion object {
        const val NoStepIndex = -1
    }
}

sealed interface GuideKitNavigationResult {
    data class StepChanged(val index: Int) : GuideKitNavigationResult
    data object Finished : GuideKitNavigationResult
    data object Skipped : GuideKitNavigationResult
    data object NoSteps : GuideKitNavigationResult
}

internal data class GuideKitResolvedStep(
    val index: Int,
    val step: GuideKitStep,
    val isLastStep: Boolean,
    val primaryButtonText: String,
    val arrowConfig: GuideKitArrowConfig,
    val targetHighlight: GuideKitTargetHighlightStyle,
    val instructionBox: GuideKitInstructionBoxStyle,
    val autoScroll: GuideKitAutoScrollConfig,
    val highlightBounds: Rect?,
)

internal fun resolveGuideKitStep(
    steps: List<GuideKitStep>,
    requestedStepIndex: Int,
    style: GuideKitStyle,
): GuideKitResolvedStep? {
    if (steps.isEmpty()) return null

    val safeStepIndex = requestedStepIndex.coerceIn(0, steps.lastIndex)
    val step = steps[safeStepIndex]
    val targetHighlight = step.targetHighlight ?: style.targetHighlight
    val isLastStep = safeStepIndex == steps.lastIndex

    return GuideKitResolvedStep(
        index = safeStepIndex,
        step = step,
        isLastStep = isLastStep,
        primaryButtonText = step.primaryButtonText ?: if (isLastStep) "Got it" else "Next",
        arrowConfig = step.arrowConfig ?: style.arrowConfig,
        targetHighlight = targetHighlight,
        instructionBox = step.instructionBox ?: style.instructionBox,
        autoScroll = step.autoScroll,
        highlightBounds = resolveGuideKitHighlightBounds(
            targetBounds = step.targetBounds,
            enabled = targetHighlight.enabled,
            paddingPx = targetHighlight.padding.toGuideKitPx(),
            shape = targetHighlight.shape,
        ),
    )
}

internal fun resolveGuideKitHighlightBounds(
    targetBounds: Rect?,
    enabled: Boolean,
    paddingPx: Float,
    shape: GuideKitTargetHighlightShape,
): Rect? {
    val paddedBounds = targetBounds
        ?.takeIf { enabled }
        ?.inflate(paddingPx)
        ?: return null

    return when (shape) {
        GuideKitTargetHighlightShape.RoundedRect -> paddedBounds
        GuideKitTargetHighlightShape.Circle -> paddedBounds.toGuideKitCircleBounds()
    }
}

internal fun calculateGuideKitAutoScrollDelta(
    targetBounds: Rect?,
    instructionBounds: Rect?,
    enabled: Boolean,
    spacingPx: Float,
    minTopVisibleDistancePx: Float,
): Float {
    val target = targetBounds ?: return 0f
    val instruction = instructionBounds ?: return 0f
    if (!enabled) return 0f

    return if (instruction.center.y >= target.center.y) {
        val neededScroll = (target.bottom - instruction.top + spacingPx).coerceAtLeast(0f)
        val topSafeScroll = (target.top - minTopVisibleDistancePx).coerceAtLeast(0f)
        neededScroll.coerceAtMost(topSafeScroll)
    } else {
        -(instruction.bottom - target.top + spacingPx).coerceAtLeast(0f)
    }
}

internal fun Rect.toGuideKitCircleBounds(): Rect {
    val radius = width.coerceAtLeast(height) / 2f
    return Rect(
        left = center.x - radius,
        top = center.y - radius,
        right = center.x + radius,
        bottom = center.y + radius,
    )
}
