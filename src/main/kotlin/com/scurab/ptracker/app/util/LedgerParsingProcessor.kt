package com.scurab.ptracker.app.util

import com.scurab.ptracker.app.ext.replaceCoin
import com.scurab.ptracker.app.model.Transaction

class LedgerParsingProcessor : (Transaction) -> Transaction? {
    override fun invoke(t: Transaction): Transaction {
        return when {
            t.hasCoin("LUNA") -> t.replaceCoin("LUNA", "LUNC")
            else -> t
        }
    }
}
