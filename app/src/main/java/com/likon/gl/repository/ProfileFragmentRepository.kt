package com.likon.gl.repository


import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.PostModel
import com.likon.gl.paging.PostsPaging
import kotlinx.coroutines.flow.Flow


class ProfileFragmentRepository(private val db: FirebaseFirestore, private  val currentUid : String) {

    fun getResult( userId : String, roomDB : RoomDBRepository) : Flow<PagingData<PostModel>> {

        return Pager(
            config = PagingConfig(
                pageSize = NETWORK_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { PostsPaging(db, userId,currentUid, roomDB) }
        ).flow

    }

    companion object {
        private const val NETWORK_PAGE_SIZE = 10
    }



}