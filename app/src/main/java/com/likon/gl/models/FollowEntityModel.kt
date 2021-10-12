package com.likon.gl.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow")
data class FollowEntityModel(@PrimaryKey val User_id : String, @NonNull val following: Boolean, @NonNull  val follower: Boolean) {
}
