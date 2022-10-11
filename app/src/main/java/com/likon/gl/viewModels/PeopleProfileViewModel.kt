package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.FollowCountsEntityModel
import com.likon.gl.models.FollowEntityModel
import com.likon.gl.models.PostModel
import com.likon.gl.repository.PostsPagingRepository
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.flow.Flow

class PeopleProfileViewModel(val fireStore: FirebaseFirestore, private val roomDB: RoomDBRepository) : ViewModel() {

    private val postsPagingRepository = PostsPagingRepository(fireStore)
    private var   currentResult: Flow<PagingData<PostModel>>? = null

    fun mainFeeds(userId: String,  currentUid : String): Flow<PagingData<PostModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<PostModel>> = postsPagingRepository.getResult(userId, roomDB, currentUid)
                .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }

    suspend fun updateFollowCounts(userId: String, count : Int){
        roomDB.updateFollowCounts(userId, count)
    }

    suspend fun insertState(followEntityModel: FollowEntityModel){
        roomDB.insertState(followEntityModel)
    }

    fun getFollowStateFlow(userId : String) :Flow<FollowEntityModel?> {
        return roomDB.getFollowStateFlow(userId)
    }

    suspend fun getFollowState(userId : String) : FollowEntityModel? {
        return roomDB.getFollowState(userId)
    }

    suspend fun updateFollowState(userId: String, state : Int) {
        roomDB.updateFollowState(userId, state)
    }

    fun getFollowCountsFlow(userId: String): Flow<FollowCountsEntityModel?>{
        return roomDB.getFollowCountsFlow(userId)
    }


}