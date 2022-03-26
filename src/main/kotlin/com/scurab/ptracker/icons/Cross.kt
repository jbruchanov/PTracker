package com.scurab.ptracker.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

public val Icons.Filled.Cross: ImageVector
    get() {
        if (_cross != null) {
            return _cross!!
        }
        _cross = materialIcon(name = "Filled.Cross") {
            materialPath {
                moveTo(1f, 1f)
                lineTo(23f, 23f)
                close()
                moveTo(23f, 1f)
                lineTo(1f, 23f)
                close()
            }
        }
        return _cross!!
    }

private var _cross: ImageVector? = null