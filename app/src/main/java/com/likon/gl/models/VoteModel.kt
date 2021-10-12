package com.likon.gl.models

import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class VoteModel(val vote : Boolean? = null, @ServerTimestamp val time : Date? = null) {
}