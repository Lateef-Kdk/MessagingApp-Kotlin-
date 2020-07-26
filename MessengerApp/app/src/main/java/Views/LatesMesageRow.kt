package Views

import Models.ChatMessageClass
import Models.UserClass
import com.example.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessageRow(val chatMessageObj: ChatMessageClass): Item<GroupieViewHolder>(){

    //Now we have a public var that holds all my chat partners info.
    //we will use this public var in LatestMessagesAct
    var chatPartnerUser: UserClass?  = null

    override fun getLayout(): Int {
        return R.layout.latest_message_row
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.LatestMessageRow_LatestMessage_TextView_ID.text = chatMessageObj.text

        //We are trying tp put the userNames In the Latest Messages Act.
        val chatPartnerId: String
        //If the message ID matched my ID
        if(chatMessageObj.fromID == FirebaseAuth.getInstance().uid){
            //It's from me
            chatPartnerId = chatMessageObj.toID
        }else {
            //otherwise it's from someone else.
            chatPartnerId = chatMessageObj.fromID
        }

        val ref = FirebaseDatabase.getInstance().getReference("/users/$chatPartnerId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            //I think this thing gets called even if there is no data change.
            override fun onDataChange(snapshot: DataSnapshot) {
                //TODO Look up what onDataChange does! Cuz I HAVE NO IDEA!
                chatPartnerUser = snapshot.getValue(UserClass::class.java)
                viewHolder.itemView.LatestMessageRow_Username_TextView_ID.text = chatPartnerUser?.userName
                val targetImageView = viewHolder.itemView.LatestMessageRow_Picture_ImageView_ID
                Picasso.get().load(chatPartnerUser?.profileImageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground) //When we are loading in the image use R.drawable.ic_launcher_foreground as a place holder
                    .error(R.drawable.ic_launcher_foreground)   //if something goes wrong replace the image with R.drawable.ic_launcher_foreground
                    .rotate(90F) //we do rotate cuz the images were sideways cuz picasso rotates BIG images
                    .into(targetImageView);
            }

        })
    }

}