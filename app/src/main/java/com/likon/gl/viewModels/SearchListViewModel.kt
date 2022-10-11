package com.likon.gl.viewModels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.SearchPeopleListRepository

import kotlinx.coroutines.flow.Flow

class SearchListViewModel : ViewModel() {

    private val peopleListRepository = SearchPeopleListRepository(FirebaseFirestore.getInstance())
    private val currentQuery = MutableLiveData(String())
    private var   currentSearchResult: Flow<PagingData<UserInfoModel>>? = null
    private var currentQueryValue: String? = null

//    val users = currentQuery.switchMap { query ->
//            peopleListRepository.getResult(query)
//    }



//    fun setQuery(query : String){
//        currentQuery.value = query
//    }

    fun searchRepo(queryString: String, roomDBViewModel: RoomDBViewModel): Flow<PagingData<UserInfoModel>> {
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = queryString
        val newResult: Flow<PagingData<UserInfoModel>> =peopleListRepository.getResult(queryString, roomDBViewModel)
                .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }


}