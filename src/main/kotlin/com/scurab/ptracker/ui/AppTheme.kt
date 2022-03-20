package com.scurab.ptracker.ui

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.scurab.ptracker.component.compose.StateColor

@Composable
fun AppTheme(block: @Composable () -> Unit) {
    MaterialTheme(
        colors = AppTheme.MaterialColors
    ) {
        CompositionLocalProvider(
            LocalContentColor provides AppTheme.MaterialColors.onBackground,
            AppColors provides AppTheme.Colors,
            AppShapes provides AppTheme.Shapes,
            AppSizes provides AppTheme.Sizes,
        ) {
            block()
        }
    }
}

val AppColors = compositionLocalOf { AppTheme.Colors }
val AppShapes = compositionLocalOf { AppTheme.Shapes }
val AppSizes = compositionLocalOf { AppTheme.Sizes }

object AppTheme {
    val MaterialColors = Colors.DarkMaterial

    object Colors {
        val Primary = Color(0xFF546E7A)
        val Secondary = Color(0xFFFF7F00)
        val BackgroundContent = Color(0xFF2B2B2B)
        val ToDo = Color.Magenta
        val OnBackground = Color.White
        val ContentColor = StateColor(default = OnBackground, selected = Secondary)
        val ToggleButtonBackground = StateColor(default = Primary)
        val WindowEdge = Color.White

        val DarkMaterial = darkColors(
            primary = Primary,
            primaryVariant = ToDo,
            secondary = Secondary,
            secondaryVariant = ToDo,
            background = BackgroundContent,
            surface = BackgroundContent,
            onBackground = OnBackground,
            onPrimary = OnBackground,
            error = ToDo,
            onSecondary = ToDo,
            onSurface = Primary,
            onError = ToDo
        )
    }

    object Shapes {
        
    }

    object Sizes {
        val MinClickableSize = 48.dp
        val IconButtonPadding = 8.dp

        val Hairline = 1.dp
        val Space05 = 2.dp
        val Space = 4.dp
    }

    object Values {
        val DividerDefaultAlpha = 0.12f
    }
}

