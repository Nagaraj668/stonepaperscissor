package com.myspsgame.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.gson.Gson
import java.util.*
import java.util.function.Predicate
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ChoosePlayerFragment : Fragment(), ItemClickListener, ChildEventListener,
    FirebaseAuth.AuthStateListener {

    lateinit var playersRecyclerView: RecyclerView
    lateinit var adapter: PlayerAdapter
    lateinit var playersList: MutableList<OnlinePlayer>

    lateinit var playersOnlineRef: DatabaseReference
    lateinit var playerRequestReference: DatabaseReference
    lateinit var firebaseAuth: FirebaseAuth

    private val reqListener = object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            val gameRequest = dataSnapshot.getValue(GameRequest::class.java)

            println("gameRequest?.ack: ${gameRequest?.ack}")

            if (gameRequest?.ack != null) {
                if (gameRequest?.ack!!) {
                    startGaming(gameRequest)
                }
            } else if (gameRequest != null) {
                showIncomingRequestPopup(gameRequest)
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {
            println(databaseError.message)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_choose_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playersRecyclerView = view.findViewById(R.id.players)
        playersRecyclerView.layoutManager = LinearLayoutManager(context)
        playersRecyclerView.itemAnimator = DefaultItemAnimator()

        playersList = ArrayList()
        adapter = PlayerAdapter(context, this, playersList)
        playersRecyclerView.adapter = adapter

        firebaseAuth = FirebaseAuth.getInstance()
        fetchOnlinePlayers()
    }

    private fun listenForRequests(uid: String) {
        playerRequestReference = FirebaseDatabase.getInstance().getReference("game_requests")
            .child(uid)
        playerRequestReference.addValueEventListener(reqListener)
    }

    private fun startGaming(gameRequest: GameRequest) {
        val intent = Intent(activity, GameActivity::class.java)
        intent.putExtra("game_request", Gson().toJson(gameRequest))
        intent.putExtra("im_acceptor", true)
        startActivity(intent)
    }

    private fun showIncomingRequestPopup(gameRequest: GameRequest?) {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(requireActivity())
        alertDialogBuilder.setMessage("${gameRequest?.requesterName} would like to play SPS")
            .setTitle("Game Request")
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                sendAck()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                decline()
            }
        alertDialogBuilder.create().show()
    }

    private fun decline() {
        playerRequestReference.child("ack").setValue(false)
    }

    private fun sendAck() {
        playerRequestReference.child("ack").setValue(true)
    }

    private fun fetchOnlinePlayers() {
        playersOnlineRef = FirebaseDatabase.getInstance().getReference("user_presence")
        playersOnlineRef.addChildEventListener(this)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        playerRequestReference?.removeEventListener(reqListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        playersOnlineRef.removeEventListener(this)
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onItemClick(position: Int) {
        sendGameRequest(playersList[position])
    }

    private fun sendGameRequest(onlinePlayer: OnlinePlayer) {
        val gameRequest = GameRequest(
            onlinePlayer.uid!!,
            firebaseAuth.uid!!,
            firebaseAuth.currentUser?.displayName!!,
            Date().time
        )

        FirebaseDatabase.getInstance().getReference("game_requests")
            .child(onlinePlayer.uid!!)
            .setValue(gameRequest)
            .addOnCompleteListener {
                Toast.makeText(
                    requireContext(),
                    "Request sent",
                    Toast.LENGTH_SHORT
                ).show()

                val intent = Intent(activity, PendingGameAckActivity::class.java)
                intent.putExtra("player_uid", onlinePlayer.uid)
                startActivity(intent)
            }
    }

    override fun onCancelled(error: DatabaseError) {
        println(error.message)
    }

    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
    }

    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
        val onlinePlayer = snapshot.getValue(OnlinePlayer::class.java)
        addOnlinePlayer(
            onlinePlayer!!,
            SharedPrefsUtils.getString("my_uid", requireActivity())!!
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun addOnlinePlayer(onlinePlayer: OnlinePlayer, uid: String) {
        if (!onlinePlayer.uid.equals(uid)) {
            playersList.add(onlinePlayer)
            adapter.players = playersList
            adapter.notifyDataSetChanged()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onChildRemoved(snapshot: DataSnapshot) {
        val onlinePlayer = snapshot.getValue(OnlinePlayer::class.java)
        removeFromList(Predicate { uid: String -> uid == onlinePlayer?.uid })
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun removeFromList(
        predicate: Predicate<String>
    ) {
        val iterator = playersList.iterator()
        while (iterator.hasNext()) {
            val t: OnlinePlayer = iterator.next()
            if (predicate.test(t.uid.toString())) {
                iterator.remove()
            }
        }
        adapter.players = playersList
        adapter.notifyDataSetChanged()
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser != null) {
            listenForRequests(p0.currentUser?.uid!!)
        }
    }
}
