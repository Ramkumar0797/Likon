package com.likon.gl.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.likon.gl.models.*
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "RoomDBViewModel"
class RoomDBViewModel(private val roomDBRepository: RoomDBRepository) : ViewModel() {



     fun getFollowStateFlow(userId : String) :Flow<FollowEntityModel?> {
        return roomDBRepository.getFollowStateFlow(userId)
    }

     fun getUploadContentFlow(postId: String) : Flow<UploadEntityModel?>{
        return roomDBRepository.getUploadContentFlow(postId)
    }

    fun getVoteFlow(postId: String) : Flow<VotesEntityModel?>{
        return roomDBRepository.getVoteFlow(postId)
    }

    fun getFollowCountsFlow(userId: String): Flow<FollowCountsEntityModel?>{
        return roomDBRepository.getFollowCountsFlow(userId)
    }

    fun getUsersFlow() : Flow<List<UsersEntity?>>{
        return roomDBRepository.getUsersFlow()
    }

    fun getChatFlow() : Flow<List<UsersEntity?>>{
        return roomDBRepository.getChatFlow()
    }
    suspend fun insertMgeCurrentUser(usersEntity: UsersEntity?, messageEntity: MessageEntity?,  insert : Boolean){
        roomDBRepository.insertMgeCurrentUser(usersEntity, messageEntity, insert)
    }

        suspend fun insertMgeSender(usersEntity: UsersEntity?, messageEntity: MessageEntity?){
        roomDBRepository.insertMgeSender(usersEntity, messageEntity)
    }

    suspend fun updateMgeCountNull(uid : String?){
        roomDBRepository.updateMgeCountNull(uid)
    }


    suspend fun updateUser(usersEntity: UsersEntity){
        roomDBRepository.updateUser(usersEntity)
    }

    suspend fun updateFollowCounts(userId: String, count : Int){
        roomDBRepository.updateFollowCounts(userId, count)
    }

    suspend fun insertFollowCounts( followCounts: List<FollowCountsEntityModel>){
        roomDBRepository.insertFollowCounts(followCounts)
    }

    suspend fun getFollowState(userId : String) : FollowEntityModel? {
        return roomDBRepository.getFollowState(userId)
    }


    suspend fun insertState(followEntityModel: FollowEntityModel){
        roomDBRepository.insertState(followEntityModel)
    }

    suspend fun updateFollowState(userId: String, state : Int) {
        roomDBRepository.updateFollowState(userId, state)
    }


    suspend fun insertUpload(uploadEntityModel: UploadEntityModel){
        roomDBRepository.insertUpload(uploadEntityModel)
    }

    suspend fun getUploads() : Flow<List<UploadEntityModel?>>{
        return  roomDBRepository.getUploads()
    }
     suspend fun getUpload() : UploadEntityModel? {

        return  roomDBRepository.getUpload()
    }

    fun updateUpload(progress : Int, status : Boolean, postId : String) = viewModelScope.launch{
        roomDBRepository.updateUpload(progress, status, postId)
    }

    suspend fun deleteUpload(){
        roomDBRepository.deleteUpload()
    }

    suspend fun insertVote(votesEntityModel: VotesEntityModel){
        roomDBRepository.insertVote(votesEntityModel)
    }

    suspend fun updateVoteState(postID: String, vote : Boolean){
        roomDBRepository.updateVoteState(postID, vote)
    }

    suspend fun updateVote(postID: String,vote : Boolean?, count : Int){
        roomDBRepository.updateVote(postID, vote, count)
    }


    fun getMessage(conversationID : String): Flow<PagingData<MessageEntity>> {

       return roomDBRepository.getMessage(conversationID)
    }

    suspend fun deleteMessage(){
        roomDBRepository.deleteMessage()
    }

}