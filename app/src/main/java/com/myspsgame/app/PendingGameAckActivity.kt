package com.myspsgame.app

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class PendingGameAckActivity : AppCompatActivity() {

    private lateinit var playerUid: String
    private lateinit var gameReqRef: DatabaseReference

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

    private fun declined() {
        Toast.makeText(applicationContext, "declined", Toast.LENGTH_SHORT).show()
    }

    private fun acknowledged() {
        Toast.makeText(applicationContext, "acknowledged", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pending_game_ack)

        playerUid = intent.getStringExtra("player_uid").toString()
        println("playerUid: $playerUid")
        gameReqRef = FirebaseDatabase.getInstance().getReference("game_requests")
            .child(playerUid).child("ack")
    }

    override fun onStart() {
        super.onStart()
        gameReqRef.addValueEventListener(ackValueListener)
    }

    override fun onStop() {
        super.onStop()
        gameReqRef.removeEventListener(ackValueListener)
    }
}