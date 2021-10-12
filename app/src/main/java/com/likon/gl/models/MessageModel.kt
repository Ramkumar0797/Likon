package com.likon.gl.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*


data class MessageModel(val message_id : String? = null,
                        val sender :String? = null,
                        val con_with : String? = null,
                        val message : String? = null,
                        val seen : Boolean = false,
                        val read : Boolean = false,
                        val received : Boolean = false,
                        @ServerTimestamp val date: Date? = null) {
}

