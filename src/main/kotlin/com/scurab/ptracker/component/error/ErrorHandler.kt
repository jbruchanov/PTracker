package com.scurab.ptracker.component.error

interface ErrorHandler {
    fun showErrorDialog(msg: String, title: String? = null, exception: Throwable? = null)
}