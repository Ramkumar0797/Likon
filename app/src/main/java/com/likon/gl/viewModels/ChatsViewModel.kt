package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import com.likon.gl.models.UsersEntity
import com.likon.gl.repository.RoomDBRepository
import kotlinx.coroutines.flow.Flow

class ChatsViewModel(private val roomDBRepository: RoomDBRepository) : ViewModel() {

    fun getChatFlow() : Flow<List<UsersEntity?>> {
        return roomDBRepository.getChatFlow()
    }
}