package messagesPackage

import modelsPackage.UserClass
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
    private var TAG = "NewMessageActTag"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

        supportActionBar?.title="Select User" //change the title at the top of the act layout

        fetchUsers()    //we need to populate the recycler view with our users
                        //SO we need to fetch their info from firebase
    }

    companion object{
        val USER_KEY = "USER_KEY"
    }

    private fun fetchUsers(){
        val usersRef = FirebaseDatabase.getInstance().getReference("/users") //get database reference to users node
        usersRef.addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) { //gets called when you retrieve users from the Database
                val secondAdapter = GroupAdapter<GroupieViewHolder>()

                snapshot.children.forEach(){
                    //this loops through all the users in our database. "it" represents all the users in the user node.
                    Log.d(TAG,it.toString())    //prints out all the users Info

                    val userClassObj = it.getValue(UserClass::class.java) //UserClass was created in the Register Act

                    if(userClassObj != null && userClassObj.uid != FirebaseAuth.getInstance().uid){
                        secondAdapter.add(UserItemClass(userClassObj))
                        //add the user into to the adapter.
                        //the adapter it contained inside the recycle view
                    }

                    //Make it so that when you click on one of the rows, you are taken to a new activity.
                    secondAdapter.setOnItemClickListener { item, view ->
                                                            //Here "item" represents UserItemCLass, but in order for use to utilize it
                                                            //we need to cast it as such. We do that in the next line.

                        val userItem = item as UserItemClass    //Now we get can get access to UserClass attributes

                        //go to the next activity
                        val myChatLogIntent = Intent(view.context,ChatLogActivity::class.java)
                        //myChatLogIntent.putExtra(USER_KEY,item.userClassObj2.userName)      //USER_KEY Was Made In Companion Object Above
                        myChatLogIntent.putExtra(USER_KEY,userItem.userClassObj2)       //Thanks to making the UserClass Parcelable,
                                                                                        //I can pass a WHOLE OBJECT in an intent
                        startActivity(myChatLogIntent)

                        finish()    //removes the current Act from back stack, so now you'll prolly
                                    //go back to the LatestMessagesAct
                    }
                }
                NewMessage_RecycleView_ID.adapter = secondAdapter
            } //By the end of this method, the recycler view is populated with all the images and what not.



        })
    }
}

class UserItemClass(val userClassObj2: UserClass): Item<GroupieViewHolder>(){
    //this Object hold the information for the user_row_new_message layout.
    //userClassObj2 represents the User and hold their information.

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        val textViewInUserRowLayout = viewHolder.itemView.NewMessage_UserName_TextView_ID
        val imageViewInUserRowLayout = viewHolder.itemView.NewMesage_ImageView_ID

        textViewInUserRowLayout.text = userClassObj2.userName
        Picasso.get().load(userClassObj2.profileImageUrl)
            .placeholder(R.drawable.ic_launcher_foreground) //When we are loading in the image use R.drawable.ic_launcher_foreground as a place holder
            .error(R.drawable.ic_launcher_foreground)   //if something goes wrong replace the image with R.drawable.ic_launcher_foreground
            .rotate(90F) //we do rotate cuz the images were sideways cuz picasso rotates BIG images
            .into(imageViewInUserRowLayout);
        //TODO This makes all the pictures rotate 90 degrees Clockwise. I need to find a way to make it so that it only rotates images that are really big.

    }

    override fun getLayout(): Int{ // this method returns an Int
        return R.layout.user_row_new_message
    }
    //the layout will render the rows in our recycler view

}