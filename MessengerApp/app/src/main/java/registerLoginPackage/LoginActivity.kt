package registerLoginPackage

import messagesPackage.LatestMessageActivity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    private val TAG = "LoginActTag"
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        supportActionBar?.hide()    //hide the fat bar at the top of the app/
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN) // Hide the status bar



        auth = FirebaseAuth.getInstance()   //this was originally auth=Firebase.auth, but that gave us errors

        Login_Button_ID.setOnClickListener {
            Log.d(TAG,"Someone Clicked the Login Button")
            login()
        }

        BackToRegistration_TextView_ID.setOnClickListener {
            Log.d(TAG,"Go back to the Registration Screen")
            val myRegistrationIntent = Intent(this, RegisterActivity::class.java)
            clearPreviousActs(myRegistrationIntent)
            startActivity(myRegistrationIntent)
            overridePendingTransition(R.anim.enter_from_left,R.anim.exit_to_right)
                                    //new page entrance     ,Old page exit
        }
    }

    private fun login(){
        if(LoginEmail_EditText_ID.text.isEmpty()){
            Toast.makeText(this,"Enter Email",Toast.LENGTH_SHORT).show()
            return
        }

        if(LoginPassword_EditText_ID.text.isEmpty()){
            Toast.makeText(this,"Enter Password",Toast.LENGTH_SHORT).show()
            return
        }

        val email = LoginEmail_EditText_ID.text.toString()
        val password = LoginPassword_EditText_ID.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if (it.isSuccessful) {
                    //"it" represents the task result. So if "it" succeeds, then the task succeeded and if it fails... the task failed.
                    Log.d(TAG, "We have signed in successfully!")
                    val user = auth.currentUser
                    Toast.makeText(this,"Login Successful!",Toast.LENGTH_SHORT).show()
                    val latestMessagesIntent = Intent(this,LatestMessageActivity::class.java)
                    clearPreviousActs(latestMessagesIntent)
                    startActivity(latestMessagesIntent)
                    overridePendingTransition(R.anim.grow,R.anim.shrink)
                                            //new page entrance     ,Old page exit

                }
        }.addOnFailureListener {
                Log.w(TAG, "Sign in Failed: ${it.message}")
                //it.message is a detailed message on why the sign in failed.
                Toast.makeText(baseContext, "Authentication failed. Try Again Good Buddy", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearPreviousActs(intent: Intent){
        Log.d(TAG,"Previous Acts GONE!")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}

