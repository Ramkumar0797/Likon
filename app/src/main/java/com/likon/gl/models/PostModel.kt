package com.likon.gl.models

import android.os.Parcelable
import java.util.*

@kotlinx.parcelize.Parcelize
data class PostModel(val image_url : String? = null ,
                val content_id : String? = null ,
                val description : String? = null,
                val date_created : Date? = null,
                val type : Long = 0,
                     val up : Long = 0,
                     val down : Long = 0,
                val comments : Long = 0,
                val votes : Long = 0) : Parcelable