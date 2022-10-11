package com.likon.gl.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.likon.gl.models.UserInfoModel

import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

private const val TAG = "SearchPeoplePaging"

class VoteListPaging (private val db: FirebaseFirestore, private val userId : String, private val postId : String) : PagingSource<QuerySnapshot, UserInfoModel>() {


    override fun getRefreshKey(state: PagingState<QuerySnapshot, UserInfoModel>): QuerySnapshot? {
        return null
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, UserInfoModel> {

        return try {


            val currentPage : QuerySnapshot


            try {
                currentPage = params.key ?: db.collection("users").document(userId).collection("posts")
                        .document(postId).collection("votes").limit(10).get().await()
            }catch(e : FirebaseFirestoreException){

                Log.d(TAG, "load: fffffffffffffffffffffffffffffffffffffffffffffffffff")
                throw e
            }

                val lastSeenDocumentSnapShot =  currentPage.documents[currentPage.size() - 1]


                val nextPage = db.collection("users").document(userId).collection("posts")
                        .document(postId).collection("votes").startAfter(lastSeenDocumentSnapShot).limit(10).get().await()

                LoadResult.Page(
                        data = getData(currentPage),
                        prevKey = null,
                        nextKey = nextPage
                )


        } catch (e: Exception) {

            Log.d(TAG, "load: gggggggggggggggggggggggggggggggggggggggggggggggggggggggggg $e")

            LoadResult.Error(e)
        }
    }



    private suspend fun getData(querySnapshot: QuerySnapshot) : List<UserInfoModel>{

        val userInfoModels = ArrayList<UserInfoModel>()

        for (document in querySnapshot) {


            val userInfoModel =  db.collection("users").document(document.id).get().await().toObject(UserInfoModel ::class.java)

            userInfoModel?.let {
                Log.d(TAG, "getData: vvvvvvvvvvvvvvvvvvvvvvvvvvvvv")
                userInfoModels.add(it)
            }


        }
        Log.d(TAG, "getData: ssssssssssssssss ${userInfoModels.size}")
        return userInfoModels
    }

}