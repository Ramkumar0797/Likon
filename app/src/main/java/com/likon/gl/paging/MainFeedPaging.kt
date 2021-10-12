package com.likon.gl.paging

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.firebase.firestore.*
import com.likon.gl.models.*
import com.likon.gl.viewModel.RoomDBViewModel
import kotlinx.coroutines.tasks.await
import java.lang.NullPointerException

private const val TAG = "MainFeedPaging"
class MainFeedPaging(private val db: FirebaseFirestore, private val currentUserId : String, private val localDB : RoomDBViewModel) : PagingSource<QuerySnapshot, PostWithUserInfoModel>() {

    private var checker : Boolean = false
    private val limit : Long = 3

    override fun getRefreshKey(state: PagingState<QuerySnapshot, PostWithUserInfoModel>): QuerySnapshot? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<QuerySnapshot>): LoadResult<QuerySnapshot, PostWithUserInfoModel> {

        return try {

            val currentPage : QuerySnapshot

            try {
                if(params.key == null){
                    checker = true
                }
                currentPage = params.key ?: db.collection("users/$currentUserId/follow")
                              .whereEqualTo("following",true) .limit(limit).get(Source.SERVER).await()

            }catch(e : FirebaseFirestoreException){
                throw e
            }

            if(params.key == null && currentPage.size() == 0){
                throw NullPointerException()
            }

            val lastSeenDocumentSnapShot =  currentPage.documents[currentPage.size() - 1]

            val nextPage = db.collection("users/$currentUserId/follow")
                .whereEqualTo("following",true).startAfter(lastSeenDocumentSnapShot) .limit(limit).get().await()

            LoadResult.Page(
                    data = getData(currentPage),
                    prevKey = null,
                    nextKey = nextPage
            )
        } catch (e: Exception) {
            Log.d(TAG, "load: ffffffffffffffffffffffffffffffffffff $e")
            LoadResult.Error(e)
        }

    }

    private suspend fun getData(querySnapshot: QuerySnapshot) : List<PostWithUserInfoModel>{

         val postWithUserInfoModels = ArrayList<PostWithUserInfoModel>()
//        postWithUserInfoModels.clear()

        if(checker) {
            currentUserId.let {

                val userInfoModel =
                    db.document("users/$it").get().await().toObject(UserInfoModel::class.java)

                val postModels =
                    db.collection("users/$it/posts").orderBy("date_created").limit(limit)
                        .get().await().toObjects(PostModel::class.java)

                for (post in postModels) {

                    post.content_id?.let { pid ->

                        val voteModel =
                            db.document("users/$it/posts/$pid/votes/$currentUserId").get().await()
                                .toObject(VoteModel::class.java)
                        val action =
                            { vote: Boolean? -> VotesEntityModel(post_id = pid, vote, post.votes) }

                        if (voteModel?.vote != null) {
                            if (voteModel.vote) {
                                localDB.insertVote(action(true))
                            } else {
                                localDB.insertVote(action(false))
                            }

                        } else {
                            localDB.insertVote(action(null))
                        }
                        userInfoModel?.let {it1->
                            postWithUserInfoModels.add(
                                PostWithUserInfoModel(
                                    it1,
                                    post
                                )
                            )
                        }

                    }
                }

            }
        }

        for (document in querySnapshot) {
            Log.d(TAG, "getData: dddddddddddddddd ${document.id}")
//            getData(document.id)

            document.id.let {

                val userInfoModel = db.document("users/$it").get().await().toObject(UserInfoModel::class.java)

                val postModels =  db.collection("users/$it/posts").orderBy("date_created").limit(limit)
                    .get().await().toObjects(PostModel::class.java)

                for (post in postModels) {

                    post.content_id?.let { pid ->

                        val voteModel =  db.document("users/$it/posts/$pid/votes/$currentUserId").get().await().toObject(VoteModel::class.java)
                        val action = {vote : Boolean? -> VotesEntityModel(post_id = pid, vote, post.votes)}

                        if(voteModel?.vote != null){
                            if (voteModel.vote){
                                localDB.insertVote(action(true))
                            }else{
                                localDB.insertVote(action(false))
                            }

                        }else{
                            localDB.insertVote(action(null))
                        }
                        userInfoModel?.let { postWithUserInfoModels.add(PostWithUserInfoModel(it, post)) }

                    }
                }

            }

        }

        return postWithUserInfoModels
    }

//    private suspend fun getData(uid : String){
//
//        val userInfoModel = db.document("users/$uid").get().await().toObject(UserInfoModel::class.java)
//
//        val postModels =  db.collection("users/$uid/posts").orderBy("date_created").limit(limit)
//            .get().await().toObjects(PostModel::class.java)
//
//        for (post in postModels) {
//
//            post.content_id?.let { pid ->
//
//                val voteModel =  db.document("users/$uid/posts/$pid/votes/$currentUserId").get().await().toObject(VoteModel::class.java)
//                val action = {vote : Boolean? -> VotesEntityModel(post_id = pid, vote, post.votes)}
//
//                if(voteModel?.vote != null){
//                    if (voteModel.vote){
//                        localDB.insertVote(action(true))
//                    }else{
//                        localDB.insertVote(action(false))
//                    }
//
//                }else{
//                    localDB.insertVote(action(null))
//                }
//                userInfoModel?.let { postWithUserInfoModels.add(PostWithUserInfoModel(it, post)) }
//
//            }
//        }
//    }

}