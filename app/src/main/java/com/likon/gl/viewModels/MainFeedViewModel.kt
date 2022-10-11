package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.FollowCountsEntityModel
import com.likon.gl.models.PostWithUserInfoModel
import com.likon.gl.models.VotesEntityModel
import com.likon.gl.repository.MainFeedPagingRepository
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.flow.Flow

class MainFeedViewModel(val fireStore: FirebaseFirestore, private val roomDB: RoomDBRepository) : ViewModel() {

    private val mainFeedPagingRepository = MainFeedPagingRepository(fireStore )
    private var   currentResult: Flow<PagingData<PostWithUserInfoModel>>? = null

    fun mainFeeds(userId: String): Flow<PagingData<PostWithUserInfoModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<PostWithUserInfoModel>> = mainFeedPagingRepository.getResult(userId, roomDB)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }

    suspend fun insertFollowCounts( followCounts: List<FollowCountsEntityModel>){
        roomDB.insertFollowCounts(followCounts)
    }

    fun getVoteFlow(postId: String) : Flow<VotesEntityModel?>{
        return roomDB.getVoteFlow(postId)
    }

    suspend fun updateVoteState(postID: String, vote : Boolean){
        roomDB.updateVoteState(postID, vote)
    }

    suspend fun updateVote(postID: String,vote : Boolean?, count : Int){
        roomDB.updateVote(postID, vote, count)
    }
}