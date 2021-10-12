package com.likon.gl.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.paging.FollowPeoplePaging
import com.likon.gl.viewModel.RoomDBViewModel

import kotlinx.coroutines.flow.Flow

class FollowPeopleListRepository(private val db: FirebaseFirestore) {

    fun getResult( userId : String, query : String,  localDB : RoomDBViewModel) : Flow<PagingData<UserInfoModel>> {

        return Pager(
                config = PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { FollowPeoplePaging(db, userId, query, localDB) }
        ).flow

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 2
    }
}