package com.likon.gl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModel.RoomDBViewModel


class RoomDBViewModelFactory(private val roomDBRepository: RoomDBRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(RoomDBViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RoomDBViewModel(roomDBRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")

    }
}