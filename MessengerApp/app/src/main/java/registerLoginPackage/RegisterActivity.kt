package registerLoginPackage


import messagesPackage.LatestMessageActivity
import modelsPackage.UserClass
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.io.IOException
import java.util.*


class RegisterActivity : AppCompatActivity() {

    private val TAG = "RegisterActTag"

    private lateinit var auth: FirebaseAuth
    private var mySelectedPhotoUri: Uri? = null

    companion object{
        //image pick code
        private val SELECT_PICTURE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        Log.d(TAG,"Welcome to Registration Act")
        supportActionBar?.hide()    //hide the fat bar at the top of the app/
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN) // Hide the status bar


        auth = FirebaseAuth.getInstance()
        Register_Button_ID.setOnClickListener {
            //TODO After they push the register button, make sure they can't
            //push it again or make a loading screen pop up.
            registerUser()
        }

        AlreadyHaveAccount_TextView_ID.setOnClickListener {
            Log.d("MainActTag", "Go to Login Activity")
            val myLoginIntent = Intent(this, LoginActivity::class.java)
            clearPreviousActs(myLoginIntent)
            startActivity(myLoginIntent)
            overridePendingTransition(R.anim.static_animation,R.anim.static_animation)
        }

        ProfileCircle_ImageView_ID.setOnClickListener {
            findOutIfWeHavePermission()
        }

        SelectPhoto_TextView_ID.setOnClickListener {
            findOutIfWeHavePermission()
        }
    }

    private fun findOutIfWeHavePermission(){
        Log.d(TAG,"Photo Select Text Selected")
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //check runtime permission
            if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                //if we don't have permission yet.

                val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)

                //ASK FOR PERMISSION
                //show popup to request rumtime permission.
                requestPermissions(permissions,
                    PERMISSION_CODE
                ) //I assume that this calls onRequestPermissionsResult()
            }else {
                //permission already granted
                pickImageFromGallery();
            }
        }else {
            //System OS is <= Marshmallow
            pickImageFromGallery();
        }
    }

    private fun registerUser(){
        if(userName_EditText_ID.text.toString().isEmpty()){
            userName_EditText_ID.error = "Please Enter a Username"
            userName_EditText_ID.requestFocus()
            return  //If any error occurs we need to stop the execution
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(Email_EditText_ID.text.toString()).matches()){       //if email is invalid.
            Email_EditText_ID.error = "Please Enter a valid Email Address"
            Email_EditText_ID.requestFocus()
            return
        }

        if(Password_EditText_ID.text.toString().isEmpty()){
            Password_EditText_ID.error = "Please Enter a Password"
            Password_EditText_ID.requestFocus()
            return
        }

        if(mySelectedPhotoUri == null){
            Toast.makeText(this,"Please select a photo.",Toast.LENGTH_SHORT).show()
            return
        }

        val email = Email_EditText_ID.text.toString()
        val password = Password_EditText_ID.text.toString()

        Log.d(TAG, "Email is $email")  //+ is used to concatenate small numbers of strings
        Log.d(TAG,"Password is $password") //$ is basically using the append method from string builder.


        //BELOW IS WHERE WE AUTHENTICATE THE USER AND MAKE SURE THEIR CREDENTIALS ARE LEGIT.
        //REGISTERING

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "Successfully Created User with uid: ${task.result?.user?.uid}")
                    //val user = auth.currentUser

                    //If everythng is successful, disable the register button so that
                    //they can't accidentally register multiple times.
                    Register_Button_ID.isClickable = false

                    //Call
                    uploadImageToFireBase()
                    //Call

                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "ERROR, ${task.exception} shoes")
                    Toast.makeText(baseContext, "${task.exception}", Toast.LENGTH_SHORT).show()
                }
            }

    }

    //Here we upload the profile pictures to firebase.
    private fun uploadImageToFireBase() {
        if(mySelectedPhotoUri == null){
            Log.d(TAG,"The Selected Photo Uri Was null")
            //TODO THEN GIVE THEM A STOCK PHOTO

        }else {
            //Upload the image to firebase
            val fileName = UUID.randomUUID().toString()
            val ref = FirebaseStorage.getInstance().getReference("/images/$fileName")
            ref.putFile(mySelectedPhotoUri!!).addOnSuccessListener {
                Log.d(TAG, "Successfully uploaded image: ${it.metadata?.path}")

                //Get the image link back from firebase. We will need it for Later
                ref.downloadUrl.addOnSuccessListener {
                    //I think "it" represents the location of the file. AKA the URL
                    Log.d(TAG, "File Location: $it")
                    saveUserToFirebaseDatabase(it.toString())
                    Log.d(TAG, "it.toString(): $it")
                }.addOnFailureListener {
                    Log.d(TAG, "THERE IS NO File Location")
                }
                //Get the image link back from firebase.  We will need it for Later
            }.addOnFailureListener {
                Log.d(TAG, "Something went wrong with the image upload")
            }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String) {
        Log.d(TAG, profileImageUrl)
        val uid = FirebaseAuth.getInstance().uid?:""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val newUserRef = UserClass(
            uid,
            userName_EditText_ID.text.toString(),
            profileImageUrl
        )

        ref.setValue(newUserRef).addOnSuccessListener {
            Toast.makeText(this,"Registration Successful",Toast.LENGTH_SHORT).show()
            val myLatestMessagesIntent = Intent(this,
                LatestMessageActivity::class.java)

            //clear off all the previous activities off your activity stack
            clearPreviousActs(myLatestMessagesIntent)
            //clear off all the previous activities off your activity stack

            startActivity(myLatestMessagesIntent)
            Log.d(TAG,"New User is added to the database")

        }.addOnFailureListener {
            Log.d(TAG,"New User was not added to the database")
        }
    }

    private fun clearPreviousActs(intent: Intent){
        Log.d(TAG,"Previous Acts GONE!")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    //Assuming this is called after requestPermissions. THIS IS US ASKING FOR PERMISSION
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted.
                    pickImageFromGallery()
                }else {
                    //permission from popup denied
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun pickImageFromGallery(){
        //Intent to pick Image
        val myPickImageIntent = Intent(Intent.ACTION_GET_CONTENT)   //this gives you options
        myPickImageIntent.type = "image/*"
        startActivityForResult(myPickImageIntent,
            SELECT_PICTURE
        )
    }

    //This is immediately called after startActivityForResult
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK && data != null){
            //If the request for the gallery is approved
            Log.d(TAG,"Photo was selected")
            try{
                mySelectedPhotoUri = data.data
                //ProfilePhoto_ImageView_ID.setImageURI(mySelectedPhotoUri) //set the photo
                ProfileCircle_ImageView_ID.setImageURI(mySelectedPhotoUri)
                Log.d(TAG,"The Image Should be there now")
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }


//END OF CLASS!
}


//TODO make it to that if they don't choose a photo, they just get a stock photo. [HOLDING OFF ON THAT ONE lol]
//TODO Fix the error toasts, they show way too much information
