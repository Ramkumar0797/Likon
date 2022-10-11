package com.likon.gl.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.PostWithUserInfoModel
import com.likon.gl.paging.MainFeedPaging
import com.likon.gl.viewModels.RoomDBViewModel

import kotlinx.coroutines.flow.Flow

class MainFeedPagingRepository(private val db: FirebaseFirestore) {

    fun getResult( userId : String, roomDBRepository: RoomDBRepository) : Flow<PagingData<PostWithUserInfoModel>> {

        return Pager(
                config = PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { MainFeedPaging(db, userId, roomDBRepository) }
        ).flow

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }
}