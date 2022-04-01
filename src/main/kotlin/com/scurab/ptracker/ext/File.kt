package com.scurab.ptracker.ext

import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

fun File.image() = Image.makeFromEncoded(readBytes()).toComposeImageBitmap()