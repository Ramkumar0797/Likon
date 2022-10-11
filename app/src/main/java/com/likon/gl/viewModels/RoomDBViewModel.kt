package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.likon.gl.models.*
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

private const val TAG = "RoomDBViewModel"
class RoomDBViewModel(private val roomDBRepository: RoomDBRepository) : ViewModel() {



     fun getUploadContentFlow(postId: String) : Flow<UploadEntityModel?>{
        return roomDBRepository.getUploadContentFlow(postId)
    }


    fun getUsersFlow() : Flow<List<UsersEntity?>>{
        return roomDBRepository.getUsersFlow()
    }


    suspend fun insertMgeCurrentUser(usersEntity: UsersEntity?, messageEntity: MessageEntity?,  insert : Boolean){
        roomDBRepository.insertMgeCurrentUser(usersEntity, messageEntity, insert)
    }

        suspend fun insertMgeSender(usersEntity: UsersEntity?, messageEntity: MessageEntity?){
        roomDBRepository.insertMgeSender(usersEntity, messageEntity)
    }


    suspend fun updateUser(usersEntity: UsersEntity){
        roomDBRepository.updateUser(usersEntity)
    }


    suspend fun insertFollowCounts( followCounts: List<FollowCountsEntityModel>){
        roomDBRepository.insertFollowCounts(followCounts)
    }


    suspend fun insertUpload(uploadEntityModel: UploadEntityModel){
        roomDBRepository.insertUpload(uploadEntityModel)
    }


     suspend fun getUpload() : UploadEntityModel? {

        return  roomDBRepository.getUpload()
    }

    fun updateUpload(progress : Int, status : Boolean, postId : String) = viewModelScope.launch{
        roomDBRepository.updateUpload(progress, status, postId)
    }

}