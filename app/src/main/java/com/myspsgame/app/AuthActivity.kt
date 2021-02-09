package com.myspsgame.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_auth.*

class AuthActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private val signIn: Int = 1
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_sign_out) {
            auth.signOut()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        auth.removeAuthStateListener(this)
    }

    fun signInGoogle(view: View) {
        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(),
            signIn
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == signIn) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(applicationContext, "Login Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        var user = firebaseAuth.currentUser;
        if (user != null) {
            authenticated(user);
        } else {
            unauthenticated();
        }
    }

    private fun unauthenticated() {
        signin_btn.isEnabled = true
    }

    private fun authenticated(user: FirebaseUser) {
        signin_btn.isEnabled = false
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
    }

    private fun goOnline(user: FirebaseUser) {
        var presenceReference = FirebaseDatabase.getInstance().getReference("user_presence")
            .child(user.uid)

        val onlinePlayer = OnlinePlayer(user.uid, user.displayName!!)
        presenceReference.setValue(onlinePlayer).addOnCompleteListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        presenceReference.onDisconnect().removeValue()
    }
}
