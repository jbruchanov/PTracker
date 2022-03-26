package com.scurab.ptracker.ui.model

import androidx.compose.ui.text.AnnotatedString

data class TransactionTextPrices(
    val buy: AnnotatedString?,
    val sell: AnnotatedString?,
    val fee: AnnotatedString? = null,
)