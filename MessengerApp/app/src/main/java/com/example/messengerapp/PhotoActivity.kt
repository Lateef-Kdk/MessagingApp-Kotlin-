package com.example.messengerapp

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.photo.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PhotoActivity : AppCompatActivity(){

    private var TAG = "PhotoActTag"
    companion object{
        //image pick code
        private val SELECT_PICTURE = 1000
        //Permission code
        private val PERMISSION_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.photo)


        button2.setOnClickListener {
            Log.d(TAG,"Select Photo")
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                //check runtime permission
                if(checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED){
                    //permission denied
                    val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    //show popup to request rumtime permission
                    requestPermissions(permissions, PERMISSION_CODE) //I assume that this calls onRequestPermissionsResult()
                }else {
                    //permission already granted
                    pickImageFromGallery2();
                }
            }else {
                //SYstem OS is <= Marshmallow
                pickImageFromGallery2();
            }
        }
    }

    //Assuming this is called after requestPermissions
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            PERMISSION_CODE -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //permission from popup granted.
                    pickImageFromGallery2()
                }else {
                    //permission from popup denied
                    Toast.makeText(this,"Permission Denied",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun pickImageFromGallery2(){
        //Intent to pick Image
        //val myPickImageIntent = Intent(Intent.ACTION_PICK)  //this opens the gallery straight up
        val myPickImageIntent = Intent(Intent.ACTION_GET_CONTENT)   //this gives you options
        myPickImageIntent.type = "image/*"
        //startActivityForResult(Intent.createChooser(myPickImageIntent,"Select Image"), SELECT_PICTURE)
        startActivityForResult(myPickImageIntent, SELECT_PICTURE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == SELECT_PICTURE && resultCode == Activity.RESULT_OK && data != null){
            Log.d(TAG,"Photo was selected")
            try{
                val uri = data.data
                button3.setImageURI(uri)
                Log.d(TAG,"The Image Should be there now")
            }catch (e: IOException){
                e.printStackTrace()
            }
        }
    }





//END OF CLASS
}