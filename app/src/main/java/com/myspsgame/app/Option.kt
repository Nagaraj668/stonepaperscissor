package com.myspsgame.app

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class Option(
    var option: Int? = null,
    var timestamp: Long? = null
)