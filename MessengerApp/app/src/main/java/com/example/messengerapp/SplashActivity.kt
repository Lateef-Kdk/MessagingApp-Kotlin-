package com.example.messengerapp

import Messages.LatestMessageActivity
import RegisterLogin.RegisterActivity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        handler = Handler()
        handler.postDelayed({
            verifyUserIsLoggedIn()
        }, 1500)


    }

    private fun verifyUserIsLoggedIn() {
        val userID = FirebaseAuth.getInstance().uid
        if(userID == null){ //aka they are not logged in
            Log.d(LatestMessageActivity.TAG,"User ID Is Null AKA User is NOT logged in")
            val backToRegisterIntent = Intent(this,
                RegisterActivity::class.java)
            clearPreviousActs(backToRegisterIntent)
            startActivity(backToRegisterIntent)
        }else {
            val latestMessagesIntent = Intent(this,
                LatestMessageActivity::class.java)
            clearPreviousActs(latestMessagesIntent)
            startActivity(latestMessagesIntent)
        }
    }

    private fun clearPreviousActs(intent: Intent){
        Log.d(LatestMessageActivity.TAG,"Previous Acts GONE!")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}