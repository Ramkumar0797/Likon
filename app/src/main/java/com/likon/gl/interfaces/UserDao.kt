package com.likon.gl.interfaces

import android.util.Log
import androidx.paging.PagingSource
import androidx.room.*
import com.likon.gl.models.*

import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    //follow state
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowState(vararg followState: FollowEntityModel)

    @Query("SELECT * FROM follow where user_id = :userId ")
    fun getFollowStateFlow(userId: String): Flow<FollowEntityModel?>

    @Query("SELECT * FROM follow where user_id = :userId ")
    suspend fun getFollowState(userId: String?): FollowEntityModel?

    @Query("UPDATE  follow SET following = :state  where user_id = :userId ")
    suspend fun updateFollowState(userId: String, state : Int)





    // follow counts
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowCounts( followCounts : List<FollowCountsEntityModel>)

    @Query("SELECT * FROM follow_counts where user_id = :userId ")
    fun getFollowCountsFlow(userId: String): Flow<FollowCountsEntityModel?>

    @Query("UPDATE  follow_counts SET followers = followers + :count  where user_id = :userId ")
    suspend fun updateFollowCounts(userId: String, count : Int)



    //uploads
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUploadContent(vararg uploads: UploadEntityModel)

    @Query("SELECT * FROM upload where status = 0 ORDER BY date ASC LIMIT 1 ")
    suspend fun getUploadContent(): UploadEntityModel?

    @Query("SELECT * FROM upload where post_id = :postID ")
    fun getUploadContentFlow(postID: String): Flow<UploadEntityModel?>

    @Query("SELECT * FROM upload where status = 0")
    fun getUploadContentsFlow(): Flow<List<UploadEntityModel?>>

    @Query("UPDATE  upload SET progress = :progress, status = :status where post_id = :postID ")
    suspend fun updateUploadContent(progress: Int, status: Boolean, postID: String)

    @Query("DELETE FROM upload")
    suspend fun deleteUpload()

    //votes
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVote(vararg votesEntityModel: VotesEntityModel)

    @Query("SELECT * FROM votes where post_id = :postID ")
    fun getVoteFlow(postID: String): Flow<VotesEntityModel?>

    @Query("UPDATE  votes SET vote = :vote where post_id = :postID ")
    suspend fun updateVoteState(postID: String, vote : Boolean)

    @Query("UPDATE  votes SET votes_count = votes_count + :count, vote = :vote where post_id = :postID ")
    suspend fun updateVote(postID: String, vote : Boolean?, count : Int)

//    @Update(entity = UploadEntityModel::class)
//    fun updateUploadContent()

    //message
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertMessage( messageEntity: MessageEntity?) : Long

    @Query("SELECT * FROM message where con_with = :ID order by sort asc")
    fun getMessages(ID: String): PagingSource<Int, MessageEntity>

    @Query("DELETE FROM message")
    suspend fun deleteMessage()

    @Query("UPDATE  message SET received =:received, seen =:seen, date =:date  where message_id = :mid ")
    suspend fun updateMessage(mid: String, received : Boolean, seen : Boolean, date : Long?)


    //users
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertUser(vararg usersEntity: UsersEntity?)

    @Query("SELECT * FROM users where username =:s")
    fun getUserFlow(s : String =""): Flow<List<UsersEntity?>>

    @Query("SELECT * FROM users where username IS NOT NULL")
    fun getChatFlow(): Flow<List<UsersEntity?>>

    @Query("UPDATE  users SET username =:username, profile_image =:profile, gender =:gender where user_id = :uid ")
    suspend fun updateUser(uid: String, username : String?, profile : String?, gender : String?)

    @Query("UPDATE  users SET unseen_count = unseen_count + 1, last_mge =:mge, sender =:uid  where user_id = :uid ")
    suspend fun updateMgeCount(uid : String?, mge : String?) : Int

    @Query("UPDATE  users SET unseen_count = 0 where user_id = :uid ")
    suspend fun updateMgeCountNull(uid : String?)

    @Query("UPDATE  users SET last_mge =:mge, sender =:sender, view_state =:state where user_id = :uid ")
    suspend fun updateLastMge(uid : String, mge : String?, sender : String?, state : String?) : Int

    @Transaction
    suspend fun insertMgeCurrentUser(usersEntity: UsersEntity?, messageEntity: MessageEntity?, insert : Boolean){
        if(insert){
          val i =  insertMessage(messageEntity)
            if(i == -1L){
                messageEntity?.let { updateMessage(it.message_id, it.received, it.seen, it.date) }
            }
        }else{
            messageEntity?.let { updateMessage(it.message_id, it.received, it.seen, it.date) }
        }

        usersEntity?.let {
            val i = updateLastMge(it.user_id, it.last_mge, it.sender, it.view_State)
            if(i <= 0){
                insertUser(usersEntity)
            }
        }

    }

    @Transaction
    suspend fun insertMgeSender(userId: String?, usersEntity: UsersEntity?, messageEntity: MessageEntity?){

        insertMessage(messageEntity)
        val i = updateMgeCount(userId, usersEntity?.last_mge)
        if(i <= 0){
            insertUser(usersEntity)
        }
    }

//    suspend fun updateUser(usersEntity: Array<out UsersEntity>)
}