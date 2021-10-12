package com.likon.gl.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.FollowPeopleListRepository

import kotlinx.coroutines.flow.Flow

class FollowListViewModel : ViewModel() {

    private val followPeopleListRepository = FollowPeopleListRepository(FirebaseFirestore.getInstance())
    private var   currentResult: Flow<PagingData<UserInfoModel>>? = null

    fun followList(userId: String, query: String, localDB : RoomDBViewModel): Flow<PagingData<UserInfoModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<UserInfoModel>> =followPeopleListRepository.getResult(userId, query, localDB)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }




}