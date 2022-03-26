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
                moveTo(1f, 12f)
                lineToRelative(23f, 0f)
                lineTo(12f, 23f)
                close()
            }
        }
        return _triangleDown!!
    }

private var _triangleDown: ImageVector? = null