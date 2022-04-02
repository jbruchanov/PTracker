package com.scurab.ptracker.ext

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import org.jetbrains.skia.Image
import java.io.File

fun File.image(): ImageBitmap = Image.makeFromEncoded(readBytes()).toComposeImageBitmap()
fun File.imageOrNull(): ImageBitmap? = if (exists() && length() > 0) kotlin.runCatching { Image.makeFromEncoded(readBytes()).toComposeImageBitmap() }.getOrNull() else null