package com.likon.gl.models



import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

@kotlinx.parcelize.Parcelize
data class UserInfoModel(val username: String? = null, val user_id: String? = null, val ful_name: String? = null,
                         val gender: String? = null, val iconic_status: String? = null, val date_of_birth: Date? = null,
                         val followers: Long = 0, val following: Long = 0, val spd: Date? = null,
                         val posts: Long = 0, @ServerTimestamp val registered_date: Date? = null,
                         val profile_image: String? = null) :
    Parcelable {
}