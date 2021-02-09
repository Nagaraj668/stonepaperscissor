package com.myspsgame.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson

class GameActivity : AppCompatActivity() {

    private lateinit var gameReq: GameRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val flag = intent.getBooleanExtra("im_acceptor", false)
        val gameReqStr = intent.getStringExtra("game_request")
        gameReq = Gson().fromJson(gameReqStr, GameRequest::class.java)

        if (flag) {
            FirebaseDatabase.getInstance().getReference("game_requests")
                .child(gameReq.uid!!)
                .removeValue()
        } else {

        }
    }
}