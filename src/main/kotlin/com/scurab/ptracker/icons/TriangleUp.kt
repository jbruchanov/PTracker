package com.scurab.ptracker.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.TriangleUp: ImageVector
    get() {
        if (_triangleUp != null) {
            return _triangleUp!!
        }
        _triangleUp = materialIcon(name = "Filled.TriangleUp") {
            materialPath {
                moveTo(1f, 12f)
                lineToRelative(23f, 0f)
                lineTo(12f, 1f)
                close()
            }
        }
        return _triangleUp!!
    }

private var _triangleUp: ImageVector? = null