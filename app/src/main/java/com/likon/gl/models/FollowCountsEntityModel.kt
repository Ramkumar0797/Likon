package com.likon.gl.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "follow_counts")
data class FollowCountsEntityModel(@PrimaryKey val User_id : String,
                                   @NonNull val followings : Long,
                                   @NonNull val posts : Long,
                                   @NonNull  val followers : Long)
