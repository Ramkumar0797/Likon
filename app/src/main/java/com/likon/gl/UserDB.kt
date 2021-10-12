package com.likon.gl

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.likon.gl.interfaces.UserDao
import com.likon.gl.models.*


private const val TAG = "UserDB"

@Database(entities = [FollowEntityModel::class,
    UploadEntityModel ::class,
    VotesEntityModel ::class,
    MessageEntity ::class, FollowCountsEntityModel ::class
    , UsersEntity ::class], version = 1, exportSchema = false)
public abstract class User : RoomDatabase() {

    abstract fun userDao() : UserDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: User? = null

        fun getDatabase(context: Context): User {

            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    User::class.java,
                    "user_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}