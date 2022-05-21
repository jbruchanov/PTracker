package com.scurab.ptracker.component.error

import com.scurab.ptracker.app.repository.AppSettings
import javax.swing.JOptionPane
import javax.swing.SwingUtilities

class JavaErrorHandler(private val settings: AppSettings) : ErrorHandler {

    override fun showErrorDialog(msg: String, title: String?, exception: Throwable?) {
        SwingUtilities.invokeLater {
            JOptionPane.showMessageDialog(null, errorMessage(msg, exception), title ?: "Error", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun errorMessage(msg: String, exception: Throwable?): String {
        return exception?.let { msg + "\n${"-".repeat(64)}\n" + it.message + if (settings.debug) "\n" + it.stackTraceToString() else "" } ?: msg
    }
}