package com.scurab.ptracker.component

import com.scurab.ptracker.app.repository.AppSettings
import com.scurab.ptracker.ui.English
import com.scurab.ptracker.ui.Texts

class TextsProvider(private val settings: AppSettings) {

    fun texts(): Texts = English
}
