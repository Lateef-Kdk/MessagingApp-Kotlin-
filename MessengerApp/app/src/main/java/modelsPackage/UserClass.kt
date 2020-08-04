package modelsPackage

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserClass(val uid: String, val userName:String, val profileImageUrl: String): Parcelable {
    //make the UserClass Parcelable means we can pass user objects from one Act to the next.
    //Doing the whole Parcelable thing is usually a hassle, but Thanks to
    //    androidExtensions {
    //        experimental = true
    //    }
    //in my Module: app gradle, it's really freaking EASY! =D
    //SO.... YEAH! lol

    constructor():this("","","")
}
