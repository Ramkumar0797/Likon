package com.likon.gl.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.paging.VoteListPaging

import kotlinx.coroutines.flow.Flow

class VotesPagingRepository(private val db: FirebaseFirestore) {


    fun getResult( userId : String, postId : String) : Flow<PagingData<UserInfoModel>> {

        return Pager(
                config = PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { VoteListPaging(db, userId, postId) }
        ).flow

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }
}