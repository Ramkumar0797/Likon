package com.likon.gl.viewModel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.VotesPagingRepository

import kotlinx.coroutines.flow.Flow

class VoteListViewModel : ViewModel() {

    private val votesPagingRepository = VotesPagingRepository(FirebaseFirestore.getInstance())
    private var   currentSearchResult: Flow<PagingData<UserInfoModel>>? = null
    private var currentQueryValue: String? = null


    fun getVotes(userId : String, postId : String): Flow<PagingData<UserInfoModel>> {
        val lastResult = currentSearchResult
        if (lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<UserInfoModel>> =votesPagingRepository.getResult(userId, postId)
                .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }


}