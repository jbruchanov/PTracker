package com.scurab.ptracker.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.TriangleDown: ImageVector
    get() {
        if (_triangleDown != null) {
            return _triangleDown!!
        }
        _triangleDown = materialIcon(name = "Filled.TriangleDown") {
            materialPath {
                moveTo(3f, 3f)
                lineToRelative(20f, 0f)
                lineTo(12f, 20f)
                close()
            }
        }
        return _triangleDown!!
    }

private var _triangleDown: ImageVector? = null