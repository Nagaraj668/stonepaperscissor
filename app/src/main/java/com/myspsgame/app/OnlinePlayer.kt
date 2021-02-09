package com.myspsgame.app

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class OnlinePlayer(var uid: String? = null, var playerName: String? = null)