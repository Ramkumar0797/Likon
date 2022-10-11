package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.FollowPeopleListRepository
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.flow.Flow

class FollowListViewModel(val fireStore: FirebaseFirestore, private val roomDBRepository: RoomDBRepository) : ViewModel() {

    private val followPeopleListRepository = FollowPeopleListRepository(fireStore)
    private var   currentResult: Flow<PagingData<UserInfoModel>>? = null

    fun followList(userId: String, query: String): Flow<PagingData<UserInfoModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<UserInfoModel>> =followPeopleListRepository.getResult(userId, query, roomDBRepository)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }




}