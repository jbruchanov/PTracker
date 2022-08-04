package com.scurab.ptracker.component.util

import java.security.MessageDigest

object Hashing {
    fun md5(value: String): String {
        val md = MessageDigest.getInstance("MD5")
        val array = md.digest(value.toByteArray())
        val sb = StringBuffer()
        for (i in array) {
            sb.append(Integer.toHexString((i.toInt() and 0xFF) or 0x100).substring(1, 3))
        }
        return sb.toString()
    }
}