package com.scurab.ptracker.component.picker

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOCase
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileFilter

class JavaFilePicker : FilePicker {

    private val _flowResult = MutableSharedFlow<FilePickerResult>(extraBufferCapacity = 16)
    override val flowResult = _flowResult.asSharedFlow()

    override fun openFilePicker(key: String, filter: String?) {
        JFileChooser().apply {
            fileFilter = object : FileFilter() {
                override fun accept(f: File): Boolean = f.isDirectory || FilenameUtils.wildcardMatch(f.name, filter, IOCase.INSENSITIVE)
                override fun getDescription(): String = filter ?: ""
            }
            val returnVal: Int = showOpenDialog(null)
            val uri = if (returnVal == JFileChooser.APPROVE_OPTION) selectedFile.absolutePath else null
            _flowResult.tryEmit(FilePickerResult(key, uri))
        }
    }
}
