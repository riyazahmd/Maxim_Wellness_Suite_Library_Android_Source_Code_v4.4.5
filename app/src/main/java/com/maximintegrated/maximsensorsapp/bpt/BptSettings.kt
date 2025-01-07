package com.maximintegrated.maximsensorsapp.bpt

import com.chibatching.kotpref.KotprefModel
import java.util.*

object BptSettings : KotprefModel() {
    var currentUser by stringPref(default = "")
    val users by stringSetPref{
        return@stringSetPref TreeSet<String>()
    }
}