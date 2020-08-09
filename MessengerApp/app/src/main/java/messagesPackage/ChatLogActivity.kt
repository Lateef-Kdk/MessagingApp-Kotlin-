package messagesPackage

import android.content.Intent
import modelsPackage.ChatMessageClass
import modelsPackage.UserClass
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLogActTag"
    }


    val myAdapter = GroupAdapter<GroupieViewHolder>()
    var toUser: UserClass? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        ChatLog_RecyclerView_ID.adapter = myAdapter

        //val userNameAtTheTop = intent.getStringExtra(NewMessageActivity.USER_KEY)
        toUser = intent.getParcelableExtra<UserClass>(NewMessageActivity.USER_KEY)      //get the receiving user object from the intent.
        supportActionBar?.title = toUser?.userName


        listenForMessages()

        ChatLogSend_Button_ID.setOnClickListener {
            Log.d(TAG,"Send the message!")
            sendTheMessage()
        }
    }

    private fun listenForMessages() {
        //val fbDbRef = FirebaseDatabase.getInstance().getReference("/messages")
        val fromIdMine = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val fbDbRef = FirebaseDatabase.getInstance().getReference("/user-messages/$fromIdMine/$toId")

        //allows is to listen for new node that pop up under the messages node. The list of messages
        //in the app will be able to automatically refresh itself using this listener
        fbDbRef.addChildEventListener(object: ChildEventListener{
            //I guess this function on called when the messages node gets a new child
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val newChatMessage = snapshot.getValue(ChatMessageClass::class.java)

                if(newChatMessage != null ) {
                    Log.d(TAG, newChatMessage.text)

                    //this is us adding messages to the recyclerview.
                    //If the message Send BY ME --> TO SOMEONE!
                    if(newChatMessage.fromID == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessageActivity.currentUser ?: return
                        myAdapter.add(ChatSentItemClass(newChatMessage.text,currentUser))           //set it to the right
                    }else {
                        myAdapter.add(ChatReceivedItemClass(newChatMessage.text,toUser!!))       //If it's FROM SOMEONE ---> TO ME!. Set it to the left
                    }
                }else{
                    Log.d(TAG, "newChatMessage was null inside ChatLogAct")
                }

                ChatLog_RecyclerView_ID.scrollToPosition(myAdapter.itemCount - 1 )
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
            }

            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
            }


        })

    }



    private fun sendTheMessage() {
        //We will save all our messages inside the FirebaseDatabase
        //We will create a new node called messages!
        Log.d(TAG,"Sending message")
        val textITyped = ChatLog_EditText_ID.text.toString()
        //get the text that the user typed

        //This is the current users ID in Firebase
        val fromIdMine = FirebaseAuth.getInstance().uid ?:  return
                                                        //"?: return" if fromID is null then we return
        //get the ID of the person you want to send a message to.
        val receivingUserObjFrmIntent = intent.getParcelableExtra<UserClass>(NewMessageActivity.USER_KEY)
        supportActionBar?.title = receivingUserObjFrmIntent?.userName
        if(receivingUserObjFrmIntent == null){
            Log.d(TAG,"UserObjFrmIntent is Null in ChatLogAct-> sendTheMessage()")
            return
        }
        val toId = receivingUserObjFrmIntent.uid


        //this creates a path for a new node called messages and give us access to it. Push allows us to create
        //new nodes.
//        val fbDbRef = FirebaseDatabase.getInstance().getReference("/messages").push()
      val fbDbRef = FirebaseDatabase.getInstance().getReference("/user-messages/$fromIdMine/$toId").push()

        val tofbDbRef = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromIdMine").push()

        //create a message obj that hold all the attributes of the chat like the text, time , and what not
        val chatMessageObj = ChatMessageClass(fbDbRef.key!!,textITyped,fromIdMine,toId,System.currentTimeMillis()/10000)
                                                                                            //dividing milisecs by 100 gives you seconds

        //then add it to the FBDB
        fbDbRef.setValue(chatMessageObj).addOnSuccessListener {
            Log.d(TAG,"Saved our chat message for sender: ${fbDbRef.key}")
            ChatLog_EditText_ID.text.clear()
            ChatLog_RecyclerView_ID.scrollToPosition(myAdapter.itemCount-1)
        }

        tofbDbRef.setValue(chatMessageObj).addOnSuccessListener {
            Log.d(TAG,"Saved our chat message for receiver: ${fbDbRef.key}")
        }

        //This gets us a reference to a new Database Node. Here we DONT push since we want the more recent message!
        val latestMessageFbDBRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromIdMine/$toId")
        //this puts something in that spot. That something being, the chatMessageObj with all it's info.
        latestMessageFbDBRef.setValue(chatMessageObj)


        //This gets us a reference to a new Database Node
        val latestMessageToFbDBRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromIdMine")
        //this puts something in that spot. That something being, the chatMessageObj with all it's info.
        latestMessageToFbDBRef.setValue(chatMessageObj)
    }

}

//Layout for messages FROM SOMEONE
class ChatReceivedItemClass(val receivedText: String, val sendingUser: UserClass): Item<GroupieViewHolder>(){
    //When you make a ViewHolder you need a layout resoure to go with it.
    override fun getLayout(): Int {
        return R.layout.chat_from_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.FromRow_TextView_ID.text = receivedText

        //load out user image into the star
        val receivingUserUri = sendingUser.profileImageUrl
        val targetImageView = viewHolder.itemView.FromRow_ImageView_ID
/*        Picasso.get().load(receivingUserUri)
            .placeholder(R.drawable.ic_launcher_foreground) //When we are loading in the image use R.drawable.ic_launcher_foreground as a place holder
            .error(R.drawable.ic_launcher_foreground)   //if something goes wrong replace the image with R.drawable.ic_launcher_foreground
            .rotate(90F) //we do rotate cuz the images were sideways since picasso rotates BIG images
            .into(targetImageView);*/

        Picasso.get().load(receivingUserUri)
            .placeholder(R.drawable.ic_launcher_foreground) //When we are loading in the image use R.drawable.ic_launcher_foreground as a place holder
            .error(R.drawable.ic_launcher_foreground)   //if something goes wrong replace the image with R.drawable.ic_launcher_foreground
            .into(targetImageView);
        //TODO This makes all the pictures rotate 90 degrees Clockwise. I need to find a way to make it so that it only rotates images that are really big.
    }
}

//Layout for Messages TO SOMEONE
class ChatSentItemClass(val sentText: String, val currentUser: UserClass): Item<GroupieViewHolder>(){
    //When you make a ViewHolder you need a layout resoure to go with it.
    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.ToRow_TextView_ID.text = sentText

        //load out user image into the star
        val receivingUserUri = currentUser.profileImageUrl
        val targetImageView = viewHolder.itemView.ToRow_ImageView_ID
        Picasso.get().load(receivingUserUri)
            .placeholder(R.drawable.ic_launcher_foreground) //When we are loading in the image use R.drawable.ic_launcher_foreground as a place holder
            .error(R.drawable.ic_launcher_foreground)   //if something goes wrong replace the image with R.drawable.ic_launcher_foreground
            .into(targetImageView)
    }
}

//TODO and copy and paste options when you hold on a message

//TODO make it scroll down whenever you get a new message.