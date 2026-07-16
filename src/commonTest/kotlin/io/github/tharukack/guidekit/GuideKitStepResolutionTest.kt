package io.github.tharukack.guidekit

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GuideKitStepResolutionTest {
    @Test
    fun emptyStepsResolveToNullLikeGuideKitNoOp() {
        val resolved = resolveGuideKitStep(
            steps = emptyList(),
            requestedStepIndex = 0,
            style = GuideKitStyle(),
        )

        assertNull(resolved)
    }

    @Test
    fun invalidStepIndexIsClampedToAvailableStep() {
        val steps = listOf(
            guideStep(title = "First"),
            guideStep(title = "Second"),
        )

        val belowFirst = resolveGuideKitStep(steps, requestedStepIndex = -50, style = GuideKitStyle())
        val aboveLast = resolveGuideKitStep(steps, requestedStepIndex = 50, style = GuideKitStyle())

        assertEquals(0, belowFirst?.index)
        assertEquals("First", belowFirst?.step?.title)
        assertEquals(1, aboveLast?.index)
        assertEquals("Second", aboveLast?.step?.title)
    }

    @Test
    fun stepInheritsArrowTargetAndInstructionStylesFromGuideKitStyle() {
        val style = GuideKitStyle(
            arrowConfig = GuideKitArrowConfig(
                from = GuideKitAnchor.CenterLeft,
                to = GuideKitAnchor.CenterRight,
                minVisibleDistance = 64.dp,
            ),
            targetHighlight = GuideKitTargetHighlightStyle(
                shape = GuideKitTargetHighlightShape.Circle,
                paddingPx = 16f,
            ),
            instructionBox = GuideKitInstructionBoxStyle(
                fillMaxWidth = false,
            ),
        )

        val resolved = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(guideStep(targetBounds = Rect(20f, 30f, 120f, 70f))),
                requestedStepIndex = 0,
                style = style,
            ),
        )

        assertEquals(GuideKitAnchor.CenterLeft, resolved.arrowConfig.from)
        assertEquals(GuideKitAnchor.CenterRight, resolved.arrowConfig.to)
        assertEquals(64.dp, resolved.arrowConfig.minVisibleDistance)
        assertEquals(GuideKitTargetHighlightShape.Circle, resolved.targetHighlight.shape)
        assertEquals(16f, resolved.targetHighlight.paddingPx)
        assertFalse(resolved.instructionBox.fillMaxWidth)
        assertEquals(Rect(4f, -16f, 136f, 116f), resolved.highlightBounds)
    }

    @Test
    fun stepStylesOverrideGuideKitStyleForThatStepOnly() {
        val style = GuideKitStyle(
            arrowConfig = GuideKitArrowConfig(from = GuideKitAnchor.TopCenter),
            targetHighlight = GuideKitTargetHighlightStyle(shape = GuideKitTargetHighlightShape.RoundedRect),
            instructionBox = GuideKitInstructionBoxStyle(fillMaxWidth = true),
        )
        val step = guideStep(
            targetBounds = Rect(20f, 30f, 120f, 70f),
            arrowConfig = GuideKitArrowConfig(from = GuideKitAnchor.BottomCenter),
            targetHighlight = GuideKitTargetHighlightStyle(
                shape = GuideKitTargetHighlightShape.Circle,
                paddingPx = 0f,
            ),
            instructionBox = GuideKitInstructionBoxStyle(fillMaxWidth = false),
        )

        val resolved = assertNotNull(resolveGuideKitStep(listOf(step), requestedStepIndex = 0, style = style))

        assertEquals(GuideKitAnchor.BottomCenter, resolved.arrowConfig.from)
        assertEquals(GuideKitTargetHighlightShape.Circle, resolved.targetHighlight.shape)
        assertFalse(resolved.instructionBox.fillMaxWidth)
        assertEquals(Rect(20f, 0f, 120f, 100f), resolved.highlightBounds)
    }

    @Test
    fun primaryButtonTextComesFromStepOrDefaultsByPosition() {
        val steps = listOf(
            guideStep(primaryButtonText = "Continue"),
            guideStep(primaryButtonText = null),
            guideStep(primaryButtonText = null),
        )

        val explicit = assertNotNull(resolveGuideKitStep(steps, requestedStepIndex = 0, style = GuideKitStyle()))
        val middleDefault = assertNotNull(resolveGuideKitStep(steps, requestedStepIndex = 1, style = GuideKitStyle()))
        val finalDefault = assertNotNull(resolveGuideKitStep(steps, requestedStepIndex = 2, style = GuideKitStyle()))

        assertEquals("Continue", explicit.primaryButtonText)
        assertEquals("Next", middleDefault.primaryButtonText)
        assertEquals("Got it", finalDefault.primaryButtonText)
        assertFalse(middleDefault.isLastStep)
        assertTrue(finalDefault.isLastStep)
    }

    @Test
    fun missingTargetBoundsProducesNoHighlight() {
        val resolved = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(guideStep(targetBounds = null)),
                requestedStepIndex = 0,
                style = GuideKitStyle(),
            ),
        )

        assertNull(resolved.highlightBounds)
    }

    @Test
    fun disabledTargetHighlightProducesNoHighlightEvenWhenTargetExists() {
        val resolved = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(
                    guideStep(
                        targetBounds = Rect(20f, 30f, 120f, 90f),
                        targetHighlight = GuideKitTargetHighlightStyle(enabled = false),
                    ),
                ),
                requestedStepIndex = 0,
                style = GuideKitStyle(),
            ),
        )

        assertNull(resolved.highlightBounds)
    }

    @Test
    fun autoScrollIsEnabledByDefaultButCanBeDisabledPerStep() {
        val defaultStep = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(guideStep()),
                requestedStepIndex = 0,
                style = GuideKitStyle(),
            ),
        )
        val disabledStep = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(guideStep(autoScroll = GuideKitAutoScrollConfig(enabled = false))),
                requestedStepIndex = 0,
                style = GuideKitStyle(),
            ),
        )

        assertTrue(defaultStep.autoScroll.enabled)
        assertFalse(disabledStep.autoScroll.enabled)
    }

    @Test
    fun resolvedStepFeedsProductionAutoScrollCalculation() {
        val resolved = assertNotNull(
            resolveGuideKitStep(
                steps = listOf(
                    guideStep(
                        targetBounds = Rect(0f, 100f, 100f, 200f),
                        targetHighlight = GuideKitTargetHighlightStyle(paddingPx = 0f),
                    ),
                ),
                requestedStepIndex = 0,
                style = GuideKitStyle(),
            ),
        )

        val delta = calculateGuideKitAutoScrollDelta(
            targetBounds = resolved.highlightBounds,
            instructionBounds = Rect(0f, 180f, 300f, 380f),
            enabled = resolved.autoScroll.enabled,
            spacingPx = 41f,
            minTopVisibleDistancePx = 41f,
        )

        assertEquals(59f, delta)
    }

    private fun guideStep(
        title: String = "Title",
        targetBounds: Rect? = Rect(0f, 0f, 100f, 100f),
        primaryButtonText: String? = null,
        arrowConfig: GuideKitArrowConfig? = null,
        targetHighlight: GuideKitTargetHighlightStyle? = null,
        instructionBox: GuideKitInstructionBoxStyle? = null,
        autoScroll: GuideKitAutoScrollConfig = GuideKitAutoScrollConfig(),
    ): GuideKitStep = GuideKitStep(
        targetBounds = targetBounds,
        title = title,
        description = "Description",
        primaryButtonText = primaryButtonText,
        arrowConfig = arrowConfig,
        targetHighlight = targetHighlight,
        instructionBox = instructionBox,
        autoScroll = autoScroll,
    )
}
