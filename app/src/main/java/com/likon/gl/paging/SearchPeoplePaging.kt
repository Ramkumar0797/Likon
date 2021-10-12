package com.likon.gl.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.likon.gl.models.FollowCountsEntityModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.RoomDBViewModel

import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

private const val TAG = "SearchPeoplePaging"

class SearchPeoplePaging (private val db: FirebaseFirestore,
                          private val query : String,
                          private val roomDBViewModel: RoomDBViewModel) : PagingSource<QuerySnapshot, UserInfoModel>() {


    override fun getRefreshKey(state: PagingState<QuerySnapshot, UserInfoModel>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, UserInfoModel> {
        Log.d(TAG, "load: zzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzzz")

        return try {

            if(query != ""){

                Log.d(TAG, "load: vvvvvvvvvvvvvvvvvvvvvvv $query")

                val currentPage = params.key ?: db.collection("users").orderBy("username").startAt(query)
                    .endAt(query+"\uf8ff").limit(1).get().await()

                val lastSeenDocumentSnapShot =  currentPage.documents[currentPage.size() - 1]
                
                val nextPage = db.collection("users").orderBy("username").startAt(query)
                    .endAt(query+"\uf8ff").startAfter(lastSeenDocumentSnapShot).limit(1).get().await()

                LoadResult.Page(
                        data = getData(currentPage),
                        prevKey = null,
                        nextKey = nextPage
                )
            }else{

                throw NullPointerException()
            }

        } catch (e: Exception) {
            Log.d(TAG, "load: jjjjjjjjjjjjjjjjjjjjjjjjjj $e")
            LoadResult.Error(e)

        }
    }



    private suspend fun getData(querySnapshot: QuerySnapshot) : List<UserInfoModel>{

        val tempFollowCounts = ArrayList<FollowCountsEntityModel>()

        if(!querySnapshot.metadata.isFromCache){
                    for (document in querySnapshot) {

                        document.toObject(UserInfoModel::class.java).let {
                            tempFollowCounts.add(FollowCountsEntityModel(it.user_id!!, it.following, it.posts, it.followers))
                        }
                    }
        }

        roomDBViewModel.insertFollowCounts(tempFollowCounts)
        return querySnapshot.toObjects(UserInfoModel::class.java)
    }

}