package com.myspsgame.app

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class GameRequest(
    var uid: String? = null, var uidRequester: String? = null, var requesterName: String? = null,
    var timestamp: Long? = null,
    var ack: Boolean? = null,
    var gameId: String? = null
)