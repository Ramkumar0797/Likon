package com.likon.gl.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.PostWithUserInfoModel
import com.likon.gl.repository.MainFeedPagingRepository

import kotlinx.coroutines.flow.Flow

class MainFeedViewModel : ViewModel() {

    private val mainFeedPagingRepository = MainFeedPagingRepository(FirebaseFirestore.getInstance())
    private var   currentResult: Flow<PagingData<PostWithUserInfoModel>>? = null

    fun mainFeeds(userId: String, roomDBViewModel: RoomDBViewModel): Flow<PagingData<PostWithUserInfoModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<PostWithUserInfoModel>> = mainFeedPagingRepository.getResult(userId, roomDBViewModel)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }
}