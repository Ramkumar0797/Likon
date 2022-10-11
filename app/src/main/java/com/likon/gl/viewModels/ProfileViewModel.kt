package com.likon.gl.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.ProfileFragmentRepository
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.sealedClasses.Data
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileViewModel(private  val fireStore: FirebaseFirestore,
                       private val roomDB: RoomDBRepository, private val currentUid: String)  : ViewModel() {

    private val profileFragmentRepository = ProfileFragmentRepository(fireStore, currentUid)
    private var   currentResult: Flow<PagingData<PostModel>>? = null
    private val   userInfo = MutableStateFlow<Data>(Data.Loading)

    fun mainFeeds(userId: String): Flow<PagingData<PostModel>> {
        val lastResult = currentResult
        if ( lastResult != null) {
            return lastResult
        }

        val newResult: Flow<PagingData<PostModel>> = profileFragmentRepository.getResult(userId, roomDB)
            .cachedIn(viewModelScope)
        currentResult = newResult

        return newResult
    }



   suspend fun getUserInfo() : StateFlow<Data> {

       CoroutineScope(Dispatchers.IO).launch {

           try {
               val   document = currentUid.let { fireStore.collection("users").document(it).get().await() }

               if (document != null) {

                   userInfo.emit(Data.Success(document.toObject(UserInfoModel::class.java)))
               }
           }catch (e: Exception){
               userInfo.emit(Data.Error(e))
           }

       }

       return userInfo
    }
    
}