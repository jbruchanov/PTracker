package com.scurab.ptracker.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import com.scurab.ptracker.ui.AppColors
import com.scurab.ptracker.ui.AppSizes

@Composable
fun FlatButton(
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    val background = AppColors.current.ButtonBackground.get()
    val tint = AppColors.current.Content.get()
    Surface(
        modifier = Modifier
            .background(background)
            .clickable(onClick = onClick)
            .defaultMinSize(minWidth = AppSizes.current.MinClickableSize, minHeight = AppSizes.current.MinClickableSize)
            .padding(AppSizes.current.IconButtonPadding)
    ) {
        Image(
            imageVector,
            contentDescription = "",
            colorFilter = ColorFilter.tint(color = tint),
            modifier = Modifier.background(background)
        )
    }
}