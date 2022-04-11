package com.scurab.ptracker.app.ext

fun Boolean.sign() = if (this) 1f else -1f
fun Boolean.signBd() = if (this) 1.bd else (-1).bd

