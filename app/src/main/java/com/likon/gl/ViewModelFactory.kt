package com.likon.gl

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModels.*

class ViewModelFactory<D, X, U>(private val fireStore: D, private val roomDB: X, private val currentUid: U) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(RoomDBViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return RoomDBViewModel(mRoomDB) as T
            }
            modelClass.isAssignableFrom(MainFeedViewModel::class.java) -> {
                val fireStoreDB = fireStore as FirebaseFirestore
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return MainFeedViewModel(fireStoreDB, mRoomDB) as T
            }
            modelClass.isAssignableFrom(UploadingViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return UploadingViewModel(mRoomDB) as T
            }

            modelClass.isAssignableFrom(ProfileViewModel::class.java) -> {
                val fireStoreDB = fireStore as FirebaseFirestore
                val mRoomDB = roomDB as RoomDBRepository
                val mCurrentUdi = currentUid as String
                @Suppress("UNCHECKED_CAST")
                return ProfileViewModel(fireStoreDB, mRoomDB, mCurrentUdi) as T
            }

            modelClass.isAssignableFrom(ChatsViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return ChatsViewModel( mRoomDB) as T
            }

            modelClass.isAssignableFrom(FollowListViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                val fireStoreDB = fireStore as FirebaseFirestore

                @Suppress("UNCHECKED_CAST")
                return FollowListViewModel(fireStoreDB, mRoomDB) as T
            }

            modelClass.isAssignableFrom(PostViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return PostViewModel( mRoomDB) as T
            }

            modelClass.isAssignableFrom(PeopleProfileViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                val fireStoreDB = fireStore as FirebaseFirestore

                @Suppress("UNCHECKED_CAST")
                return PeopleProfileViewModel(fireStoreDB, mRoomDB) as T
            }

            modelClass.isAssignableFrom(MessageViewModel::class.java) -> {
                val mRoomDB = roomDB as RoomDBRepository
                @Suppress("UNCHECKED_CAST")
                return MessageViewModel( mRoomDB) as T
            }

        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}