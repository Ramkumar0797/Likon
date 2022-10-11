package com.likon.gl.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.paging.SearchPeoplePaging
import com.likon.gl.viewModels.RoomDBViewModel

import kotlinx.coroutines.flow.Flow

class SearchPeopleListRepository(private val db: FirebaseFirestore) {


    fun getResult( query : String, roomDBViewModel: RoomDBViewModel) : Flow<PagingData<UserInfoModel>> {

        return Pager(
                config = PagingConfig(
                        pageSize = NETWORK_PAGE_SIZE,
                        enablePlaceholders = false
                ),
                pagingSourceFactory = { SearchPeoplePaging(db,query, roomDBViewModel) }
        ).flow

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 15
    }
}