package com.scurab.ptracker.model

import kotlinx.datetime.LocalDateTime
import java.math.BigDecimal
import kotlin.random.Random

data class Transaction(
    val symbol: String,
    val time: LocalDateTime,
    val amount: BigDecimal,
    val fee: BigDecimal
)

fun randomTransactionData(count: Int, random: Random): List<Transaction> {
    return buildList<Transaction> {
        repeat(count) {
            add(
                Transaction(
                    "BTC/GBP",
                    LocalDateTime(
                        2022,
                        random.nextInt(1, 2),
                        random.nextInt(1, 28),
                        random.nextInt(1, 22),
                        random.nextInt(1, 59),
                        0,
                        0
                    ),
                    BigDecimal.valueOf(random.nextFloat() * 1000.0),
                    BigDecimal.valueOf(random.nextFloat() * 10.0)
                )
            )
        }
    }.sortedBy { it.time }
}