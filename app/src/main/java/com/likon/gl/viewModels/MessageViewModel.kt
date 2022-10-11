package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.likon.gl.models.MessageEntity
import com.likon.gl.repository.RoomDBRepository
import kotlinx.coroutines.flow.Flow

class MessageViewModel(private val roomDBRepository: RoomDBRepository) : ViewModel() {

    suspend fun updateMgeCountNull(uid : String?){
        roomDBRepository.updateMgeCountNull(uid)
    }

    fun getMessage(conversationID : String): Flow<PagingData<MessageEntity>> {

        return roomDBRepository.getMessage(conversationID)
    }
}