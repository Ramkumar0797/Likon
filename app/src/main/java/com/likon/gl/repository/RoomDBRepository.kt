package com.likon.gl.repository

import android.util.Log
import androidx.annotation.WorkerThread
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.likon.gl.interfaces.UserDao
import com.likon.gl.models.*

import kotlinx.coroutines.flow.Flow

class RoomDBRepository(private val userDao: UserDao) {


     fun getFollowStateFlow(userId : String) : Flow<FollowEntityModel?>{
        return userDao.getFollowStateFlow(userId)
    }

     fun getUploadContentFlow(postId: String) : Flow<UploadEntityModel?>{
        return userDao.getUploadContentFlow(postId)
    }

    fun getVoteFlow(postId: String) : Flow<VotesEntityModel?>{
        return userDao.getVoteFlow(postId)
    }
    fun getFollowCountsFlow(userId: String): Flow<FollowCountsEntityModel?>{
        return userDao.getFollowCountsFlow(userId)
    }

    fun getUsersFlow() : Flow<List<UsersEntity?>>{
        return userDao.getUserFlow()
    }

    fun getChatFlow() : Flow<List<UsersEntity?>>{
        return userDao.getChatFlow()
    }

    suspend fun insertMgeCurrentUser(usersEntity: UsersEntity?, messageEntity: MessageEntity?, insert : Boolean){
        userDao.insertMgeCurrentUser(usersEntity, messageEntity, insert)
    }


        suspend fun insertMgeSender(usersEntity: UsersEntity?, messageEntity: MessageEntity?){
        userDao.insertMgeSender(usersEntity?.user_id, usersEntity, messageEntity)
    }

    suspend fun updateUser(usersEntity: UsersEntity){
        userDao.updateUser(usersEntity.user_id, usersEntity.username, usersEntity.profile_image, usersEntity.gender)
    }

    suspend fun updateMgeCountNull(uid : String?){
        userDao.updateMgeCountNull(uid)
    }

    suspend fun insertFollowCounts( followCounts : List<FollowCountsEntityModel>){
        userDao.insertFollowCounts(followCounts)
    }

    suspend fun updateFollowCounts(userId: String, count : Int){
        userDao.updateFollowCounts(userId, count)
    }


    suspend fun getFollowState(userId : String) : FollowEntityModel?{
        return userDao.getFollowState(userId)
    }

    suspend fun insertState(followEntityModel: FollowEntityModel) {
         userDao.insertFollowState(followEntityModel)
    }

    suspend fun updateFollowState(userId: String, state : Int) {
        userDao.updateFollowState(userId, state)
    }

    suspend fun insertVote(votesEntityModel: VotesEntityModel){
        userDao.insertVote(votesEntityModel)
    }

     suspend fun insertUpload(uploadEntityModel: UploadEntityModel){
        userDao.insertUploadContent(uploadEntityModel)
    }

    suspend fun updateVoteState(postID: String, vote : Boolean){
        userDao.updateVoteState(postID, vote)
    }

    suspend fun updateVote(postID: String,vote : Boolean?, count : Int){
        userDao.updateVote(postID, vote, count)
    }



    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getUpload() : UploadEntityModel?{
        return  userDao.getUploadContent()
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun updateUpload(progress : Int, status : Boolean, postId : String){
        Log.d("TAG", "getUpload: vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv ${Thread.currentThread().name} ")
        userDao.updateUploadContent(progress, status, postId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    fun getUploads() : Flow<List<UploadEntityModel?>>{
        return  userDao.getUploadContentsFlow()
    }

   suspend fun deleteUpload(){
        userDao.deleteUpload()
    }

    fun getMessage(ID : String): Flow<PagingData<MessageEntity>> {

        return Pager(
            config = PagingConfig(
                pageSize = 50,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { userDao.getMessages(ID) }
        ).flow
    }


    suspend fun deleteMessage(){
        userDao.deleteMessage()
    }


}