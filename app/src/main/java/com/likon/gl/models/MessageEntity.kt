package com.likon.gl.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.firebase.firestore.ServerTimestamp


@Entity(tableName = "message", indices = [Index(value = ["message_id"], unique = true)])
data class MessageEntity(@PrimaryKey(autoGenerate = true) val sort : Int = 0,
                         val message_id : String,
                         val sender :String? = null,
                         val con_with : String? = null,
                         val message : String? = null,
                         val seen : Boolean = false,
                         val received : Boolean = false,
                         val date : Long? = null) {
}