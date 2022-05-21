package com.scurab.ptracker.component.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.scurab.ptracker.LocalWindow
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.dnd.DropTargetListener
import java.io.File

@Composable
fun rememberDropFileTarget(
    onFiles: (List<File>) -> Unit
) {
    val window = LocalWindow.current ?: return
    DisposableEffect(Unit) {
        val dropListener = object : DropTargetListener {
            override fun dragEnter(dtde: DropTargetDragEvent) {}
            override fun dragOver(dtde: DropTargetDragEvent) {}
            override fun dropActionChanged(dtde: DropTargetDragEvent) {}
            override fun dragExit(dte: DropTargetEvent) {}

            override fun drop(event: DropTargetDropEvent) {
                event.acceptDrop(DnDConstants.ACTION_LINK)
                runCatching {
                    event.transferable
                        .let { t -> t.getTransferData(t.transferDataFlavors.firstOrNull { it.isFlavorJavaFileListType }) as List<*> }
                        .filterIsInstance<File>()
                        .toList()
                }.getOrNull()
                    ?.let {
                        onFiles(it)
                    }

                event.dropComplete(true)
            }
        }
        val target = DropTarget(window, dropListener)
        onDispose {
            target.removeNotify()
        }
    }
}