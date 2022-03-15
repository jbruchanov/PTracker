package com.scurab.ptracker.ext

import java.math.BigDecimal

val String.bd: BigDecimal get() = BigDecimal(this).align