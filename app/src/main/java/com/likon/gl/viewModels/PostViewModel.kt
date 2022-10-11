package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import com.likon.gl.models.VotesEntityModel
import com.likon.gl.repository.RoomDBRepository
import kotlinx.coroutines.flow.Flow

class PostViewModel(private val roomDBRepository: RoomDBRepository) : ViewModel() {

    fun getVoteFlow(postId: String) : Flow<VotesEntityModel?> {
        return roomDBRepository.getVoteFlow(postId)
    }

    suspend fun updateVoteState(postID: String, vote : Boolean){
        roomDBRepository.updateVoteState(postID, vote)
    }

    suspend fun updateVote(postID: String,vote : Boolean?, count : Int){
        roomDBRepository.updateVote(postID, vote, count)
    }
}