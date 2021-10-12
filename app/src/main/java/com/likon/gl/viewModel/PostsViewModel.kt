package com.likon.gl.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.PostModel
import com.likon.gl.repository.PostsPagingRepository

import kotlinx.coroutines.flow.Flow

class PostsViewModel : ViewModel() {

    private val postsPagingRepository = PostsPagingRepository(FirebaseFirestore.getInstance())
    private var   currentResult: Flow<PagingData<PostModel>>? = null

    fun mainFeeds(userId: String, roomDBViewModel: RoomDBViewModel, currentUid : String): Flow<PagingData<PostModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<PostModel>> = postsPagingRepository.getResult(userId, roomDBViewModel, currentUid)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }
}