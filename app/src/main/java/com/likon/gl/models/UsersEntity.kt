package com.likon.gl.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp


@Entity(tableName = "users")
data class UsersEntity(@PrimaryKey val user_id : String,
                       val profile_image :String? = null,
                       val username : String? = null,
                       val gender : String? = null,
                       val unseen_count : Int = 0,
                       val last_mge : String? = null,
                       val sender : String?  = null,
                       val view_State : String? = null

)