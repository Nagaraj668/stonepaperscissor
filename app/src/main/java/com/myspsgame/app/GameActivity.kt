package com.myspsgame.app

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_game.*
import java.util.*

class GameActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener, ValueEventListener {

    private lateinit var gameReq: GameRequest
    private lateinit var gameIdReference: DatabaseReference
    private lateinit var opponentMoveReference: DatabaseReference
    private var flag: Boolean = false

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var uid: String

    private var myOption: Option? = null
    private var opponentOption: Option? = null

    var yourPoints: Int = 0
    var opponentPoints: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        flag = intent.getBooleanExtra("im_acceptor", false)
        val gameReqStr = intent.getStringExtra("game_request")
        gameReq = Gson().fromJson(gameReqStr, GameRequest::class.java)

        println("GameId: " + gameReq.gameId)

        gameIdReference = FirebaseDatabase.getInstance().getReference("games")
            .child(gameReq.gameId!!)

        gameIdReference.child("gameRequest").setValue(gameReq)

        if (flag) {
            FirebaseDatabase.getInstance().getReference("game_requests")
                .child(gameReq.uid!!)
                .removeValue()
            listenOpponentMove(gameReq.uidRequester)
        } else {
            listenOpponentMove(gameReq.uid)
        }
    }

    private fun listenOpponentMove(opponentUid: String?) {
        opponentMoveReference = gameIdReference.child(opponentUid!!)
        opponentMoveReference.addValueEventListener(this)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.addAuthStateListener(this)
    }

    fun stone(view: View) {
        updateOption(1)
    }

    fun paper(view: View) {
        updateOption(2)
    }

    fun scissor(view: View) {
        updateOption(3)
    }

    private fun updateOption(option: Int) {
        // your_choice.text = button.text
        myOption = Option(option, Date().time)
        gameIdReference.child(uid).setValue(myOption)
        displayYourOption()
        validate()
    }


    private fun validate() {
        if (myOption != null && opponentOption != null) {
            displayOpponentOption()
            process()
            myOption = null
            opponentOption = null
        }
    }

    private fun displayOpponentOption() {
        when (opponentOption?.option) {
            1 -> {
                opponent_choice.setImageResource(R.drawable.stone)
            }
            2 -> {
                opponent_choice.setImageResource(R.drawable.paper)
            }
            3 -> {
                opponent_choice.setImageResource(R.drawable.scissor)
            }
        }
    }

    private fun displayYourOption() {
        when (myOption?.option) {
            1 -> {
                your_choice.setImageResource(R.drawable.stone)
            }
            2 -> {
                your_choice.setImageResource(R.drawable.paper)
            }
            3 -> {
                your_choice.setImageResource(R.drawable.scissor)
            }
        }
    }

    private fun process() {
        val winnerOption = SPS(myOption?.option!!, opponentOption?.option!!).findWinner()

        if (winnerOption != 0) {
            if (winnerOption == myOption!!.option) {
                Toast.makeText(applicationContext, "You won", Toast.LENGTH_SHORT).show()
                yourPoints++
                your_points.text = "$yourPoints"
            } else {
                Toast.makeText(applicationContext, "You Lost", Toast.LENGTH_SHORT).show()
                opponentPoints++
                opponent_points.text = "$opponentPoints"
            }
        } else {
            Toast.makeText(applicationContext, "Draw", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDataChange(snapshot: DataSnapshot) {
        opponentOption = snapshot.getValue(Option::class.java)
        opponent_choice.setImageResource(R.color.teal_700)
        validate()
    }

    override fun onCancelled(error: DatabaseError) {
    }

    override fun onDestroy() {
        super.onDestroy()
        opponentMoveReference.removeEventListener(this)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        if (firebaseAuth.currentUser != null) {
            uid = firebaseAuth.currentUser!!.uid
            println("game started")
        }
    }

}