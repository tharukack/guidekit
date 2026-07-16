package io.github.tharukack.guidekit

import androidx.compose.ui.geometry.Rect
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GuideKitControllerTest {
    @Test
    fun nextStepNavigationMovesForward() {
        val controller = GuideKitController(stepCount = 3)

        val result = controller.next()

        assertEquals(GuideKitNavigationResult.StepChanged(1), result)
        assertEquals(1, controller.currentStepIndex)
        assertFalse(controller.isFirstStep)
        assertFalse(controller.isLastStep)
    }

    @Test
    fun previousStepNavigationMovesBackward() {
        val controller = GuideKitController(stepCount = 3, initialStepIndex = 2)

        val result = controller.previous()

        assertEquals(GuideKitNavigationResult.StepChanged(1), result)
        assertEquals(1, controller.currentStepIndex)
        assertFalse(controller.isFirstStep)
        assertFalse(controller.isLastStep)
    }

    @Test
    fun previousStepNavigationDoesNotMoveBeforeFirstStep() {
        val controller = GuideKitController(stepCount = 3)

        val result = controller.previous()

        assertEquals(GuideKitNavigationResult.StepChanged(0), result)
        assertEquals(0, controller.currentStepIndex)
        assertTrue(controller.isFirstStep)
    }

    @Test
    fun skipBehaviourReturnsSkippedWithoutChangingStep() {
        val controller = GuideKitController(stepCount = 3, initialStepIndex = 1)

        val result = controller.skip()

        assertEquals(GuideKitNavigationResult.Skipped, result)
        assertEquals(1, controller.currentStepIndex)
    }

    @Test
    fun completionCallbackIsRepresentedWhenNextIsPressedOnLastStep() {
        val controller = GuideKitController(stepCount = 2, initialStepIndex = 1)

        val result = controller.next()

        assertEquals(GuideKitNavigationResult.Finished, result)
        assertEquals(1, controller.currentStepIndex)
        assertTrue(controller.isLastStep)
    }

    @Test
    fun emptyStepListHasNoStepAndReturnsNoSteps() {
        val controller = GuideKitController(stepCount = 0)

        assertFalse(controller.hasSteps)
        assertEquals(GuideKitController.NoStepIndex, controller.currentStepIndex)
        assertEquals(GuideKitNavigationResult.NoSteps, controller.next())
        assertEquals(GuideKitNavigationResult.NoSteps, controller.previous())
        assertEquals(GuideKitNavigationResult.NoSteps, controller.skip())
    }

    @Test
    fun invalidInitialStepIndexIsClamped() {
        val belowFirst = GuideKitController(stepCount = 3, initialStepIndex = -10)
        val beyondLast = GuideKitController(stepCount = 3, initialStepIndex = 99)

        assertEquals(0, belowFirst.currentStepIndex)
        assertEquals(2, beyondLast.currentStepIndex)
    }

    @Test
    fun targetNotFoundReturnsNoHighlightAndNoScroll() {
        val highlightBounds = resolveGuideKitHighlightBounds(
            targetBounds = null,
            enabled = true,
            paddingPx = 10f,
            shape = GuideKitTargetHighlightShape.RoundedRect,
        )
        val scrollDelta = calculateGuideKitAutoScrollDelta(
            targetBounds = null,
            instructionBounds = Rect(0f, 200f, 300f, 360f),
            enabled = true,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertNull(highlightBounds)
        assertEquals(0f, scrollDelta)
    }

    @Test
    fun overlayDismissalUsesSkipBehaviour() {
        val controller = GuideKitController(stepCount = 2)

        assertEquals(GuideKitNavigationResult.Skipped, controller.dismissOverlay())
    }

    @Test
    fun configurationChangeCanRestoreAndClampCurrentStep() {
        val restored = GuideKitController(stepCount = 4, initialStepIndex = 2)
        val shrunkResult = restored.updateStepCount(stepCount = 2)

        assertEquals(2, GuideKitController(stepCount = 4, initialStepIndex = 2).currentStepIndex)
        assertEquals(GuideKitNavigationResult.StepChanged(1), shrunkResult)
        assertEquals(1, restored.currentStepIndex)
    }

    @Test
    fun roundedHighlightPositionAddsPadding() {
        val highlightBounds = resolveGuideKitHighlightBounds(
            targetBounds = Rect(20f, 30f, 120f, 90f),
            enabled = true,
            paddingPx = 10f,
            shape = GuideKitTargetHighlightShape.RoundedRect,
        )

        assertEquals(Rect(10f, 20f, 130f, 100f), highlightBounds)
    }

    @Test
    fun circleHighlightPositionUsesLargestSideAroundCenter() {
        val highlightBounds = resolveGuideKitHighlightBounds(
            targetBounds = Rect(20f, 30f, 120f, 70f),
            enabled = true,
            paddingPx = 0f,
            shape = GuideKitTargetHighlightShape.Circle,
        )

        assertEquals(Rect(20f, 0f, 120f, 100f), highlightBounds)
    }

    @Test
    fun disabledHighlightReturnsNull() {
        val highlightBounds = resolveGuideKitHighlightBounds(
            targetBounds = Rect(20f, 30f, 120f, 90f),
            enabled = false,
            paddingPx = 10f,
            shape = GuideKitTargetHighlightShape.RoundedRect,
        )

        assertNull(highlightBounds)
    }

    @Test
    fun autoScrollMovesDownWhenInstructionBoxOverlapsBelowTarget() {
        val delta = calculateGuideKitAutoScrollDelta(
            targetBounds = Rect(0f, 100f, 100f, 200f),
            instructionBounds = Rect(0f, 180f, 300f, 380f),
            enabled = true,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertEquals(59f, delta)
    }

    @Test
    fun autoScrollClampsDownwardScrollToKeepHighlightBelowTopEdge() {
        val delta = calculateGuideKitAutoScrollDelta(
            targetBounds = Rect(0f, 50f, 100f, 160f),
            instructionBounds = Rect(0f, 120f, 300f, 320f),
            enabled = true,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertEquals(9f, delta)
    }

    @Test
    fun autoScrollMovesUpWhenInstructionBoxOverlapsAboveTarget() {
        val delta = calculateGuideKitAutoScrollDelta(
            targetBounds = Rect(0f, 260f, 100f, 360f),
            instructionBounds = Rect(0f, 100f, 300f, 280f),
            enabled = true,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertEquals(-61f, delta)
    }

    @Test
    fun disabledAutoScrollReturnsZero() {
        val delta = calculateGuideKitAutoScrollDelta(
            targetBounds = Rect(0f, 100f, 100f, 200f),
            instructionBounds = Rect(0f, 180f, 300f, 380f),
            enabled = false,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertEquals(0f, delta)
    }

}
