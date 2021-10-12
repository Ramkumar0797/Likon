package com.likon.gl.home

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.*
import com.likon.*
import com.likon.gl.MainActivity
import com.likon.gl.MyApplication
import com.likon.gl.R
import com.likon.gl.RoomDBViewModelFactory
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.databinding.FeedsUsersAdapterBinding
import com.likon.gl.databinding.FragmentMainfeedBinding
import com.likon.gl.databinding.MainFeedsAdapterBinding
import com.likon.gl.interfaces.OnDownVoteClickedListener
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnPostItemsClickListener
import com.likon.gl.interfaces.OnUpVoteClickedListener
import com.likon.gl.models.FollowCountsEntityModel
import com.likon.gl.models.PostModel
import com.likon.gl.models.PostWithUserInfoModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.MainFeedViewModel
import com.likon.gl.viewModel.RoomDBViewModel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.NullPointerException
import kotlin.random.Random


class MainFeedFragment(private val onFragmentChangeListener: OnFragmentChangeListener) : Fragment(R.layout.fragment_mainfeed),
    OnPostItemsClickListener,
    OnUpVoteClickedListener, OnDownVoteClickedListener{

    private var _binding: FragmentMainfeedBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private val viewModel by viewModels<MainFeedViewModel>()
    private val mainFeedAdapter = MainFeeds(this, this, this)
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((mContext.application as MyApplication).repository) }
    private lateinit var currentUId : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            currentUId = context.currentUserId
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMainfeedBinding.bind(view)

        binding.apply {

            uploads.setOnClickListener{
                onFragmentChangeListener.onChange(this@MainFeedFragment, R.integer.uploads, null)
            }

            mainFeedsView.apply {
                setHasFixedSize(true)
                adapter = mainFeedAdapter.withLoadStateFooter(footer = LoadingStateAdapter{mainFeedAdapter.retry()})
            }
            getData()



            retry.setOnClickListener {
                mainFeedAdapter.retry()
            }

            mainFeedAdapter.addLoadStateListener {
                val loading = it.source.refresh is LoadState.Loading
                val error = it.source.refresh is LoadState.Error
                val errorState = it.source.append as? LoadState.Error
                        ?: it.source.prepend as? LoadState.Error
                        ?: it.append as? LoadState.Error
                        ?: it.prepend as? LoadState.Error
                        ?: it.source.refresh as? LoadState.Error
                        ?: it.refresh as? LoadState.Error

                if(errorState?.error is NullPointerException && error){
                    getUsers()
                }else{
                    noResult.isVisible =errorState?.error is NullPointerException && error
                    networkError.isVisible =errorState?.error is FirebaseFirestoreException && error
                    loader.isVisible = loading
                    mainFeedsView.isVisible = it.source.refresh is LoadState.NotLoading && !error
                }


            }

        }
    }

    private fun getUsers(){

        db.collection("users").orderBy("posts").limit(5)
            .get(Source.SERVER)
            .addOnSuccessListener {

                if(it != null){
                    binding.apply {
                        mainFeedsUsers.apply {
                            setHasFixedSize(true)
                            val users = it.toObjects(UserInfoModel::class.java)
                            adapter = FeedsUsers(users)

                            val tempFollowCounts = ArrayList<FollowCountsEntityModel>()
                            for(user in users){
                                tempFollowCounts.add(FollowCountsEntityModel(user.user_id!!,
                                    user.following, user.posts, user.followers))
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                roomDBViewModel.insertFollowCounts(tempFollowCounts)
                            }

                        }
                        mainFeedsUsers.visibility  = View.VISIBLE
                        loader.visibility = View.GONE

                    }
                }

            }
    }

    private fun getData(){



        lifecycleScope.launchWhenResumed {

                viewModel.mainFeeds(currentUId, roomDBViewModel).collectLatest { values ->
                    mainFeedAdapter.submitData(values)
                }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<PostWithUserInfoModel>() {

            override fun areItemsTheSame(oldConcert: PostWithUserInfoModel,
                                         newConcert: PostWithUserInfoModel
            ) = oldConcert.postModel.content_id == newConcert.postModel.content_id

            override fun areContentsTheSame(oldConcert: PostWithUserInfoModel,
                                            newConcert: PostWithUserInfoModel
            ) = oldConcert.postModel == newConcert.postModel
        }
    }

    inner class FeedsUsers(userInfoModels: List<UserInfoModel>) : RecyclerView.Adapter<FeedsUsers.FeedsUsersHolder>() {

        private val models : List<UserInfoModel> = userInfoModels

        inner class  FeedsUsersHolder(private val feedsUsersAdapterBinding: FeedsUsersAdapterBinding) : RecyclerView.ViewHolder(feedsUsersAdapterBinding.root), View.OnClickListener {
            fun bind(userInfoModel: UserInfoModel?){

                feedsUsersAdapterBinding.apply {

                    username .text = userInfoModel?.username
                    fulName.text = userInfoModel?.ful_name
                    val getImage = {gender : String?, profile : String? ->
                        profile ?: if(gender == "male") R.raw.male else R.raw.female
                    }

                    if(userInfoModel?.profile_image == null){
                        profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    }

                    Glide.with(itemView)
                        .load(getImage(userInfoModel?.gender, userInfoModel?.profile_image))
                        .into(profileImage)

                }
            }

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                onItemClick(R.integer.people_profile, null, models[bindingAdapterPosition] )
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsUsersHolder {
            val binding = FeedsUsersAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FeedsUsersHolder(binding)
        }

        override fun onBindViewHolder(holder: FeedsUsersHolder, position: Int) {
            holder.bind(models[position])

        }

        override fun getItemCount(): Int {
            return models.size
        }

    }

    inner class MainFeeds(val onPostItemsClick: OnPostItemsClickListener, val upVoteClicked: OnUpVoteClickedListener,
                          val downVoteClicked: OnDownVoteClickedListener) : PagingDataAdapter<PostWithUserInfoModel, MainFeeds.FeedsViewHolder>(DIFF_CALLBACK) {

        inner class FeedsViewHolder(private val mainFeedsAdapterBinding: MainFeedsAdapterBinding) : RecyclerView.ViewHolder(mainFeedsAdapterBinding.root),
                View.OnClickListener {

            fun bind(postWithUserInfoModel: PostWithUserInfoModel?) {

                mainFeedsAdapterBinding.apply {

                    lifecycleScope.launchWhenResumed {

                        postWithUserInfoModel?.postModel?.content_id?.let {
                            roomDBViewModel.getVoteFlow(it).collectLatest { value ->
                                votes.text = getString(R.string.votes, value?.votes_count)
                                val vote = {upVote : Boolean, downVote : Boolean -> up.isChecked = upVote
                                    down.isChecked = downVote }
                                when (value?.vote) {
                                    true -> {
                                        vote(true, false)
                                    }
                                    false -> {
                                        vote(false, true)
                                    }
                                    else -> {
                                        vote(false, false)
                                    }
                                }
                            }
                        }
                    }

                    postWithUserInfoModel?.let {


                        iconicStatus.setImageResource(getIconicImage(it.userInfoModel.iconic_status,
                                it.userInfoModel.gender))
                        username.text = it.userInfoModel.username
                        comments.text = getString(R.string.comments, it.postModel.comments)
                        description.text = it.postModel.description

                        val getImage = {gender : String?, profile : String? ->
                            profile ?: if(gender == "male") R.raw.male else R.raw.female
                        }

                        if(it.userInfoModel.profile_image == null){
                            profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        Glide.with(this@MainFeedFragment)
                            .load(getImage(it.userInfoModel.gender, it.userInfoModel.profile_image))
                            .into(profileImage)
                        Glide.with(itemView)
                            .load(it.postModel.image_url)
                            .into(imagePost)

                        it.postModel.type.let { type ->
                            if (type == 1L) {
                                Glide.with(itemView)
                                    .load(R.raw.leaf_fall_red)
                                    .into(background)
                                specialText.isVisible = true
                            }
                        }
                    }

                }
            }

            init {
                mainFeedsAdapterBinding.apply {
                    val setOnClick = {  views : Array<View> -> for(v in views){ v.setOnClickListener(this@FeedsViewHolder) } }
                    setOnClick(arrayOf(profileToolbar, votes, comments, up, down, addComment, result))

                }
            }

            override fun onClick(p0: View?) {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION){
                    getItem(position)?.let {
                        mainFeedsAdapterBinding.apply {
                            when(p0?.id){

                                votes.id -> onPostItemsClick.onItemClick(R.integer.votes, it.postModel, it.userInfoModel)
                                profileToolbar.id -> onPostItemsClick.onItemClick(R.integer.people_profile, null, it.userInfoModel)
                                up.id -> upVoteClicked.onUpVoteClick(up.isChecked, down.isChecked, it.postModel.content_id, it.userInfoModel.user_id)
                                down.id -> downVoteClicked.onDownVoteClick(up.isChecked, down.isChecked, it.postModel.content_id, it.userInfoModel.user_id)
                                result.id -> onPostItemsClick.onItemClick(R.integer.result, it.postModel, it.userInfoModel)
                                comments.id -> onPostItemsClick.onItemClick(R.integer.comments, it.postModel, it.userInfoModel)

                            }
                        }
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: FeedsViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsViewHolder {
            val binding = MainFeedsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FeedsViewHolder(binding)
        }

    }

    override fun onUpVoteClick(upState: Boolean, downState: Boolean, postId: String?, userId: String?) {

        if(upState && !downState){
            setData("increment", true, postId, userId )

        }else if(upState && downState){
            setData("update", true, postId, userId )

        }else if(!upState && !downState){
            setData("decrement", null, postId, userId )
        }

    }

    private fun getIconicImage(iconicStatus: String?, gender : String?): Int {

        val getImage = {temp : String? -> if(temp == "male") R.raw.msingle else R.raw.fsingle}
        return when(iconicStatus){


            "single" -> getImage(gender)

            "valentine" -> R.raw.heart

            "married" -> R.raw.couple

            else -> 0
        }

    }

    override fun onDownVoteClick(upState: Boolean, downState: Boolean, postId: String?, userId: String?) {

        if(downState && !upState){
            setData("increment", false, postId, userId )

        }else if(downState && upState){
            setData("update", false, postId, userId )

        }else if(!downState && !upState){
            setData("decrement", null, postId, userId )

        }
    }

    private fun setData(job : String, vote : Boolean?, postId: String?, userId: String?){

        CoroutineScope(Dispatchers.IO).launch {

            val setVote = {pid : String, vote : Boolean? ->
                val docPath =   db.document("users/$userId/posts/$pid/votes/$currentUId")
                    if(vote != null){
                        docPath.set(hashMapOf("vote" to vote, "time" to FieldValue.serverTimestamp()), SetOptions.merge())
                    }else{
                        docPath.delete()
                    }
            }
            when(job){

                "increment" ->{
                    postId?.let {pId -> roomDBViewModel.updateVote(pId, vote, 1)
                    setVote(pId, vote)
                    }
                }
                "decrement" ->{
                    postId?.let {pId -> roomDBViewModel.updateVote(pId, vote, -1)
                        setVote(pId, null)
                    }
                }
                "update" ->{
                    postId?.let {pId -> roomDBViewModel.updateVoteState(pId, vote!!)
                        setVote(pId, vote)
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(navigateTO: Int, postModel: PostModel?, userInfoModel: UserInfoModel?) {

        when(navigateTO){
            R.integer.people_profile -> onFragmentChangeListener.onChange(this,R.integer.people_profile,Bundle().apply { putParcelable("user info", userInfoModel) })
            R.integer.votes ->  onFragmentChangeListener.onChange(this, R.integer.votes, Bundle().apply { putStringArray("votesInfo", arrayOf(userInfoModel?.user_id, postModel?.content_id) )})
            R.integer.result ->  onFragmentChangeListener.onChange(this, R.integer.result, Bundle().apply { putStringArray("resultInfo",
                arrayOf(userInfoModel?.user_id, postModel?.content_id, postModel?.image_url) )})
            R.integer.comments ->  onFragmentChangeListener.onChange(this, R.integer.comments,
                Bundle().apply { putStringArray("commentsInfo", arrayOf(userInfoModel?.user_id, postModel?.content_id) )})

        }

    }


}
