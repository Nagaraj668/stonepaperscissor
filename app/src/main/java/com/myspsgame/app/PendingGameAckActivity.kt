package com.myspsgame.app

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import com.google.gson.Gson

class PendingGameAckActivity : AppCompatActivity() {

    private lateinit var gameRequest: GameRequest
    private lateinit var gameAckRef: DatabaseReference
    private lateinit var gameIdRef: DatabaseReference


    private val ackValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val ackFlag = snapshot.getValue(Boolean::class.java)

            if (ackFlag != null) {
                if (ackFlag) {
                    acknowledged();
                } else {
                    declined();
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.message)
        }
    }

    private val gameIdValueListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val gameId = snapshot.getValue(String::class.java)

            if (gameId != null) {
                startGaming(gameId)
            }
        }

        override fun onCancelled(error: DatabaseError) {
            println(error.message)
        }
    }

    private fun startGaming(gameId: String) {
        gameRequest.gameId = gameId
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("game_request", Gson().toJson(gameRequest))
        startActivity(intent)
        finish()
    }

    private fun declined() {
        Toast.makeText(applicationContext, "Request declined", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun acknowledged() {
        Toast.makeText(applicationContext, "Game started", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_game_ack)

        gameRequest = Gson().fromJson(
            intent.getStringExtra("game_request").toString(),
            GameRequest::class.java
        )

        gameAckRef = FirebaseDatabase.getInstance().getReference("game_requests")
            .child(gameRequest.uid!!).child("ack")

        gameIdRef = FirebaseDatabase.getInstance().getReference("game_requests")
            .child(gameRequest.uid!!).child("gameId")
    }

    override fun onStart() {
        super.onStart()
        gameAckRef.addValueEventListener(ackValueListener)
        gameIdRef.addValueEventListener(gameIdValueListener)
    }

    override fun onStop() {
        super.onStop()
        gameAckRef.removeEventListener(ackValueListener)
        gameIdRef.removeEventListener(gameIdValueListener)
    }
}
