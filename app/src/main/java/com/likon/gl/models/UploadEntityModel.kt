package com.likon.gl.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "upload")
data class UploadEntityModel(@PrimaryKey val post_id : String, val uri : String, val description : String?, val progress : Int, val date : Long, val status : Boolean) {

}