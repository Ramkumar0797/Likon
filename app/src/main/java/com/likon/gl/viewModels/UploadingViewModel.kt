package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import com.likon.gl.models.UploadEntityModel
import com.likon.gl.repository.RoomDBRepository
import kotlinx.coroutines.flow.Flow

class UploadingViewModel(private val roomDB: RoomDBRepository) : ViewModel() {

    suspend fun getUploads() : Flow<List<UploadEntityModel?>> {
        return  roomDB.getUploads()
    }
}