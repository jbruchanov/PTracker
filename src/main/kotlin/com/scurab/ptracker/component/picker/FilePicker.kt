package com.scurab.ptracker.component.picker

import kotlinx.coroutines.flow.SharedFlow

interface FilePicker {

    val flowResult: SharedFlow<FilePickerResult>

    fun openFilePicker(key: String, filter: String? = null)
}

data class FilePickerResult(
    val key: String,
    val uri: String?
)