package com.likon.gl.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.Source
import com.likon.gl.models.PostModel
import com.likon.gl.models.PostWithUserInfoModel
import com.likon.gl.models.VoteModel
import com.likon.gl.models.VotesEntityModel
import com.likon.gl.viewModel.RoomDBViewModel

import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

private const val TAG = "MainFeedPaging"
class PostsPaging(private val db: FirebaseFirestore, private val userId : String, private val currentUid : String,
                  private val localDB : RoomDBViewModel) : PagingSource<QuerySnapshot, PostModel>() {
    override fun getRefreshKey(state: PagingState<QuerySnapshot, PostModel>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, PostModel> {

        return try {


            val currentPage : QuerySnapshot

            try {
                currentPage = params.key ?: db.collection("users").document(userId).collection("posts")
                        .orderBy("date_created").limit(10).get().await()
            }catch(e : FirebaseFirestoreException){

                Log.d(TAG, "load: fffffffffffffffffffffffffffffffffffffffffffffffffff")
                throw e
            }

            if(params.key == null && currentPage.size() == 0){
                throw NullPointerException()
            }

            val lastSeenDocumentSnapShot =  currentPage.documents[currentPage.size() - 1]
            val nextPage = db.collection("users").document(userId).collection("posts")
                    .orderBy("date_created").startAfter(lastSeenDocumentSnapShot) .limit(10).get().await()

            LoadResult.Page(
                    data = getData(currentPage),
                    prevKey = null,
                    nextKey = nextPage
            )
        } catch (e: Exception) {

            Log.d(TAG, "load: eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee $e")
            LoadResult.Error(e)
        }

    }

    private suspend fun getData(querySnapshot: QuerySnapshot) : List<PostModel>{
        Log.d(TAG, "getData: 000000000000000000000000000000")
        val values = querySnapshot.toObjects(PostModel ::class.java)
        for(document in values){


            document.content_id?.let { pid ->

                val voteModel =
                    db.document("users/$userId/posts/$pid/votes/$currentUid").get().await()
                        .toObject(VoteModel::class.java)
                val action =
                    { vote: Boolean? -> VotesEntityModel(post_id = pid, vote, document.votes) }

                if (voteModel?.vote != null) {
                    if (voteModel.vote) {
                        localDB.insertVote(action(true))
                    } else {
                        localDB.insertVote(action(false))
                    }

                } else {
                    localDB.insertVote(action(null))
                }


            }

        }
        return values
    }

}