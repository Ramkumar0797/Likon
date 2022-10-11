package com.likon.gl.paging

import android.content.ContentValues
import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.likon.gl.models.FollowCountsEntityModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModels.RoomDBViewModel


import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

private const val TAG = "FollowPeoplePaging"
class FollowPeoplePaging(private val db: FirebaseFirestore,
                         private val userId : String,
                         private val query : String ,
                         private val localDB : RoomDBRepository) : PagingSource<QuerySnapshot, UserInfoModel>() {

    override fun getRefreshKey(state: PagingState<QuerySnapshot, UserInfoModel>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, UserInfoModel> {

        return try {


            val currentPage : QuerySnapshot

            try {
                currentPage = params.key ?: db.collection("users").document(userId).collection("follow")
                        .whereEqualTo(query,true) .limit(2).get(Source.SERVER).await()
            }catch(e : FirebaseFirestoreException ){

                throw e
            }

            if(params.key == null && currentPage.size() == 0){
                throw NullPointerException()
            }

            val lastSeenDocumentSnapShot =  currentPage.documents[currentPage.size() - 1]
            val nextPage = db.collection("users").document(userId).collection("follow")
                    .whereEqualTo(query,true) .startAfter(lastSeenDocumentSnapShot).limit(2).get(Source.SERVER).await()

            LoadResult.Page(
                    data = demo(currentPage),
                    prevKey = null,
                    nextKey = nextPage
            )
        } catch (e: Exception) {

            LoadResult.Error(e)
        }
    }


    private suspend fun demo(querySnapshot: QuerySnapshot) : List<UserInfoModel>{
        val tempUserInfo = ArrayList<UserInfoModel>()
        val tempFollowCounts = ArrayList<FollowCountsEntityModel>()
        for (document in querySnapshot) {
            Log.d(ContentValues.TAG, "demo: 33333333333333333333333cvnc333333 ${document.id}" )

            val demoGet =  db.collection("users").document(document.id)
                    .get(Source.SERVER).await()

            demoGet.toObject(UserInfoModel::class.java)?.let {

                tempUserInfo.add(it)
                tempFollowCounts.add(FollowCountsEntityModel(it.user_id!!, it.following, it.posts, it.followers))
            }

        }

        localDB.insertFollowCounts(tempFollowCounts)
        return tempUserInfo
    }

}