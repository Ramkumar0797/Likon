package com.likon.gl.models

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "votes")
data class VotesEntityModel(@PrimaryKey val post_id : String, @NonNull val vote: Boolean? = null, @NonNull val votes_count: Long) {
}