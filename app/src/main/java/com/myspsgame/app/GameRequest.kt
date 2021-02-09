package com.myspsgame.app

data class GameRequest(
    var uid: String? = null, var uidRequester: String? = null, var requesterName: String? = null,
    var timestamp: Long? = null,
    var ack: Boolean? = null
)