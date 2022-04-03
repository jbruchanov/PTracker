package com.scurab.ptracker.app.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Rhombus: ImageVector
    get() {
        if (_rhombus != null) {
            return _rhombus!!
        }
        _rhombus = materialIcon(name = "Filled.Rhombus") {
            materialPath {
                moveTo(12f, 1f)
                lineTo(1f, 12f)
                lineTo(12f, 23f)
                lineTo(23f, 12f)
                close()
            }
        }
        return _rhombus!!
    }

private var _rhombus: ImageVector? = null