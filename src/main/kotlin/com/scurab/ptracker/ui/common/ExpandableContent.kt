package com.scurab.ptracker.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun ExpandableContent(
    visible: Boolean = true,
    initialVisibility: Boolean = false,
    duration: Int = 300,
    content: @Composable BoxScope.() -> Unit
) {
    val enterFadeIn = remember { fadeIn(animationSpec = TweenSpec(durationMillis = duration, easing = FastOutLinearInEasing)) }
    val enterExpand = remember { expandVertically(animationSpec = tween(duration)) }
    val exitFadeOut = remember { fadeOut(animationSpec = TweenSpec(durationMillis = duration, easing = LinearOutSlowInEasing)) }
    val exitCollapse = remember { shrinkVertically(animationSpec = tween(duration)) }
    val visibleState = remember { MutableTransitionState(initialState = initialVisibility) }

    AnimatedVisibility(
        visibleState = visibleState.apply { targetState = visible },
        modifier = Modifier,
        enter = enterExpand + enterFadeIn,
        exit = exitCollapse + exitFadeOut
    ) {
        Box {
            content()
        }
    }
}
