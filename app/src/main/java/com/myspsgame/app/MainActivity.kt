package com.myspsgame.app

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.content_main)
    }

    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_sign_out -> {
                firebaseAuth.signOut()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAuthStateChanged(p0: FirebaseAuth) {
        if (p0.currentUser == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            authenticated(p0.currentUser!!)
        }
    }

    private fun authenticated(user: FirebaseUser) {
        updateProfile(user)
        goOnline(user)
        clearOldRequests(user)
    }

    private fun clearOldRequests(user: FirebaseUser) {
        val playerRequestReference = FirebaseDatabase.getInstance().getReference("game_requests")
            .child(user.uid)
        playerRequestReference.removeValue()
    }

    private fun updateProfile(user: FirebaseUser) {
        val gameUser = GameUser(user.uid, user.displayName!!, user.email!!);
        val userReference = FirebaseDatabase.getInstance().getReference("users")
            .child(user.uid);
        userReference.setValue(gameUser)
        SharedPrefsUtils.putString("my_uid", user.uid, this)
    }

    private fun goOnline(user: FirebaseUser) {
        var presenceReference = FirebaseDatabase.getInstance().getReference("user_presence")
            .child(user.uid)

        val onlinePlayer = OnlinePlayer(user.uid, user.displayName!!)
        presenceReference.setValue(onlinePlayer)
        presenceReference.onDisconnect().removeValue()
    }
}
