package com.scurab.ptracker.app.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Square: ImageVector
    get() {
        if (_square != null) {
            return _square!!
        }
        _square = materialIcon(name = "Filled.Square") {
            materialPath {
                moveTo(1f, 1f)
                lineTo(1f, 23f)
                lineTo(23f, 23f)
                lineTo(23f, 1f)
                close()
            }
        }
        return _square!!
    }

private var _square: ImageVector? = null
