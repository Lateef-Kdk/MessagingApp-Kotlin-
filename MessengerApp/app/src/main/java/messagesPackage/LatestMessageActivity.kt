package messagesPackage

import messagesPackage.NewMessageActivity.Companion.USER_KEY
import modelsPackage.ChatMessageClass
import modelsPackage.UserClass
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.example.messengerapp.R
import registerLoginPackage.RegisterActivity
import Views.LatestMessageRow
import androidx.recyclerview.widget.DividerItemDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_message.*

class LatestMessageActivity : AppCompatActivity() {

    companion object{
        var TAG = "LatestMessagesActTag"
        var currentUser: UserClass? = null
    }
    val myAdapter = GroupAdapter<GroupieViewHolder>()
    val latestMessageMap = HashMap<String,ChatMessageClass>()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"\t\tWelcome to Latest Messages Act")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_message)

        LatestMessages_RecyclerView_ID.adapter = myAdapter
        LatestMessages_RecyclerView_ID.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))



        supportActionBar?.title = "messagesPackage"

        //Make it so that when you click on someones messages in LatestMessages, you go to
        //the ChatLog
        //The adapter is the row, I guess.
        myAdapter.setOnItemClickListener { item, view ->
            Log.d(TAG,"123")
            val myChatlogActIntent = Intent(this,ChatLogActivity::class.java)
            //starting the intent isn't good enough cuz the screen won't have any messages
            //SO in addition to that we will do it the same way we did it in the NewMessages Act

            //the "item" variable up top represents each individual row and since each row is just
            //as LatestMessagesRow Act object... We cast it as such
            val row = item as LatestMessageRow
            if (row.chatPartnerUser == null){
                Log.d(TAG,"Chat partner is null")
            }
            myChatlogActIntent.putExtra(USER_KEY,row.chatPartnerUser)       //since row is a LatestMessagesRow Object and that
                                                                //and that class has a public var chatPartner. This is cool
            startActivity(myChatlogActIntent)
        }

        listenForLatestMessages()

        //we are going to need the current users information in
        //another act
        fetchCurrentUser()
        
        verifyUserIsLoggedIn()
    }

    private fun refreshRecyclerViewMessages(){
        myAdapter.clear()   //clear the adapter
        //TODO not very efficient to clear eveything. try to make targeted updates

        latestMessageMap.values.forEach(){
            myAdapter.add(LatestMessageRow(it))
                //add all the latest messages to the adapter.
        }
    }

    private fun listenForLatestMessages() {
        val fromIdMine = FirebaseAuth.getInstance().uid //AKA THE USERS ID
        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("latest-messages/$fromIdMine")
        latestMessagesRef.addChildEventListener(object: ChildEventListener{

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageClass::class.java)?: return
                latestMessageMap[snapshot.key!!] = chatMessage //the key is the recipeints userID
                refreshRecyclerViewMessages()
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessageClass::class.java)?: return
                latestMessageMap[snapshot.key!!] = chatMessage //the key is the recipeints userID
                refreshRecyclerViewMessages()
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
            override fun onCancelled(error: DatabaseError) {

            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }


        })
    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid //this is the uid of the current user
        val fbDbref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        fbDbref.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            //This is just to show that currentUser gives us access to
            //the users information. Also... onDataChange gets called
            //even if nothing changes.... WEIRD.
            override fun onDataChange(snapshot: DataSnapshot) {
                currentUser = snapshot.getValue(UserClass::class.java)
                Log.d(TAG,"Current User ${currentUser?.userName}")
                Log.d(TAG,"Current User ${currentUser?.profileImageUrl}")
                Log.d(TAG,"Current User ${currentUser?.uid}")
            }

        })
    }

    private fun verifyUserIsLoggedIn() {
        val userID = FirebaseAuth.getInstance().uid
        if(userID == null){
            Log.d(TAG,"User ID Is Null AKA User is NOT logged in")
            val backToRegisterIntent = Intent(this,
                RegisterActivity::class.java)
            clearPreviousActs(backToRegisterIntent)
            startActivity(backToRegisterIntent)
        }else {
            Log.d(TAG,"UID IS FINE! $userID")
        }
    }

    private fun clearPreviousActs(intent: Intent){
        Log.d(TAG,"Previous Acts GONE!")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){//item.id represents the ID of the menu items
            //so when the item.itemIf is  R.id.Menu_NewMessage_ID start the NewMessagesAct
            R.id.Menu_NewMessage_ID ->{
                val newMessagesIntent = Intent(this,
                    NewMessageActivity::class.java)
                startActivity(newMessagesIntent)
            }

            R.id.Menu_SignOut_ID ->{
                FirebaseAuth.getInstance().signOut()
                val backToRegisterIntent = Intent(this,
                    RegisterActivity::class.java)
                clearPreviousActs(backToRegisterIntent)
                startActivity(backToRegisterIntent)
            }
        }
                //itemId represends the ID of the menu items in the nav_menu.xml
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //this just creates the menu
        //it's like set content view for the menu
        menuInflater.inflate(R.menu.nav_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }
//TODO Include a profile pic or something somewhere!
// TODO I never know whose profile I'm on
    //TODO MAKE IT SO 2 PEopel cam't have the same username
    //end of class
}

