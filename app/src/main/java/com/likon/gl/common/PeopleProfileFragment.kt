package com.likon.gl.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.likon.*
import com.likon.gl.MainActivity
import com.likon.gl.MyApplication
import com.likon.gl.RoomDBViewModelFactory
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.databinding.FragmentPeopleProfileBinding
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.FollowEntityModel
import com.likon.gl.models.FollowStateModel
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.PostsViewModel
import com.likon.gl.viewModel.RoomDBViewModel
import com.likon.gl.R
import com.likon.gl.databinding.ContentsAdapterBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnPostItemsClickListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.NullPointerException

private const val TAG = "PeopleProfileFragment"

class PeopleProfileFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                            private val onBackPressed : OnFragmentBackPressed)
    : Fragment(R.layout.fragment_people_profile), View.OnClickListener,
    OnPostItemsClickListener {

    private var _binding: FragmentPeopleProfileBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var  userInfo : UserInfoModel? = null
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((mContext.application as MyApplication).repository) }
    private val viewModel by viewModels<PostsViewModel>()
    private lateinit var currentUserRef : DocumentReference
    private lateinit var profilerRef : DocumentReference
    private lateinit var postsListAdapter :PostsListAdapter
    private val setOnClick = {  views : Array<View> -> for(v in views){ v.setOnClickListener(this) } }
    private lateinit var currentUId : String


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            currentUId = context.currentUserId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfo = it.getParcelable("user info")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPeopleProfileBinding.bind(view)

        binding.apply {

            userInfo?.let { it ->
                postsListAdapter = PostsListAdapter(this@PeopleProfileFragment, it)
                username.text = it.username
                fulName.text = it.ful_name
                if(it.iconic_status != null){
                    iconicStatus.setImageResource(getIconicImage(it.iconic_status, it.gender))
                }
                val getImage = {gender : String?, profile : String? -> profile ?: if(gender == "male")
                    R.raw.male else R.raw.female
                }
                if(it.profile_image == null){
                    profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                }
                Glide.with(this@PeopleProfileFragment)
                    .load(getImage(it.gender, it.profile_image))
                    .into(profileImage)

                countFollowers.text = it.followers.toString()
                countFollowings.text = it.following.toString()
                countPosts.text = it.posts.toString()
                setOnClick(arrayOf(countFollowings, iconicStatus ,backArrow,textFollowers, textFollowings,
                    textPosts, message, infoRetry, countFollowers, follow, following, followBack))

                postsList.apply {

                    setHasFixedSize(true)
                    adapter = postsListAdapter.withLoadStateFooter(footer = LoadingStateAdapter{postsListAdapter.retry()})
                }

                postsListAdapter.addLoadStateListener {

                    val loading = it.source.refresh is LoadState.Loading
                    val error = it.source.refresh is LoadState.Error
                    val errorState = it.source.append as? LoadState.Error
                        ?: it.source.prepend as? LoadState.Error
                        ?: it.append as? LoadState.Error
                        ?: it.prepend as? LoadState.Error
                        ?: it.source.refresh as? LoadState.Error
                        ?: it.refresh as? LoadState.Error

                    noResult.isVisible =errorState?.error is NullPointerException && error
                    networkError.isVisible =errorState?.error is FirebaseFirestoreException && error
                    loader.isVisible = loading
                    postsList.isVisible = it.source.refresh is LoadState.NotLoading && !error
                    loadingInfo.isVisible = postsList.isVisible == false && loader.isVisible || networkError.isVisible || noResult.isVisible

                }

                it.user_id?.let { id ->

                if(auth.uid != id){


                    val generator  = { userId : String , docId : String -> db.collection("users").document(userId)
                        .collection("follow")
                        .document(docId)}
                    currentUserRef =  generator(auth.uid!!, id)
                    profilerRef = generator(id, auth.uid!!)
                    checkFollowState(id)

                }else{
                    setFollowStateVisible(null)
                    setProfileInfoVisible(id, false)

                }

                    lifecycleScope.launchWhenResumed {
                        roomDBViewModel.getFollowCountsFlow(id).collectLatest {

                            if(it != null){
                                binding.apply {
                                    countFollowers.text = getCount(it.followers)
                                    countFollowings.text = getCount(it.followings)
                                    countPosts.text = getCount(it.posts)
                                }
                            }

                        }
                    }

                }
            }
        }
    }




    private fun getPosts(userId: String, current : String){
        lifecycleScope.launchWhenResumed {
            viewModel.mainFeeds(userId, roomDBViewModel, current)
                .collectLatest { value ->
                    postsListAdapter.submitData(value)
                }
        }
    }

    private fun checkFollowState(userId: String){

        currentUserRef.get(Source.SERVER)
            .addOnSuccessListener { document ->

                if (document != null && document.exists()) {
                    val followModel = document.toObject(FollowStateModel::class.java)
                    followModel?.let {
                        setFollowState(document.id, it.following ?: false, it.follower ?: false)
                        setFollowStateListener(userId)
                    }

                } else {
                    setFollowState(document.id, following = false, follower = false)
                    setFollowStateListener(userId)
                }

            }.addOnFailureListener{
                getFollowStateFromLocal(userId)
            }
    }

    private fun setFollowState(userId : String, following : Boolean, follower : Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            roomDBViewModel.insertState(FollowEntityModel(User_id = userId, following = following, follower = follower))
        }
    }

    private fun getIconicImage(iconicStatus: String, gender : String?): Int {

        val getImage = {temp : String? -> if(temp == "male") R.raw.msingle else R.raw.fsingle}
        return when(iconicStatus){


            "single" -> getImage(gender)

            "valentine" -> R.raw.heart

            "married" -> R.raw.couple

            else -> 0
        }

    }


    private fun setFollowStateListener( userId : String){

        lifecycleScope.launchWhenResumed {
            roomDBViewModel.getFollowStateFlow(userId).collectLatest {

                if(it != null){

                    setFollowStateVisible(FollowStateModel(it.following,it.follower))
                    setProfileInfoVisible(userId, false)
                }
            }
        }

    }
    private fun getCount(count : Long) : String{
        return  if(count >= 0) count.toString() else "0"
    }


    private fun getFollowStateFromLocal( userId : String){

        CoroutineScope(Dispatchers.IO).launch {
            val followState = roomDBViewModel.getFollowState(userInfo?.user_id!!)
            if(followState != null){
                CoroutineScope(Dispatchers.Main).launch{
                    setFollowStateListener( userId)
                }
            }else{
                lifecycleScope.launchWhenResumed {
                    setProfileInfoVisible(userId, true)
                    Toast.makeText(mActivity,"Network Error!",Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setProfileInfoVisible(userId: String, profileError : Boolean){

        if(!profileError){
            binding.apply {
                profileLayout.visibility = View.VISIBLE
                infoLoader.visibility = View.GONE
            }
            getPosts(userId, currentUId)
        }else{
            binding.apply {
                profileLayout.visibility = View.GONE
                infoLoader.visibility = View.GONE
                infoRetry.visibility = View.VISIBLE
            }
        }

    }

    private fun setFollowStateVisible(followModel: FollowStateModel?){

        if(followModel != null){
            binding.apply {
                follow.isVisible = followModel.following != true && followModel.follower != true
                followingMessageLayout.isVisible = followModel.following == true
                followBack.isVisible = followModel.following != true && followModel.follower == true
            }
        }else{
            binding.apply {
                follow.visibility = View.GONE
                followingMessageLayout.visibility = View.GONE
                followBack.visibility = View.GONE
            }
        }

    }


    override fun onClick(p0: View?) {

        binding.apply {
            when (p0?.id) {

                follow.id,
                followBack.id       -> updateFollowState(true)
                following.id        -> updateFollowState(false)
                backArrow.id        -> onBackPressed.onBackPress()
                countFollowers.id,
                textFollowers.id    -> navigateTo(R.integer.follow)
                countFollowings.id,
                textFollowings.id   -> navigateTo(R.integer.follow)
                message.id          -> navigateTo(R.integer.message)
                iconicStatus.id     -> navigateTo(R.integer.iconic_status)
                infoRetry.id        -> {
                                            userInfo?.user_id?.let { checkFollowState(it) }
                                            infoLoader.visibility = View.VISIBLE
                                            infoRetry.visibility = View.GONE
                                        }

            }
        }
    }

    private fun navigateTo(fragment : Int){

            onFragmentChangeListener.onChange(this, fragment, Bundle().apply { putParcelable("user info", userInfo)})
    }

    private fun updateFollowState(state : Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            db.runBatch { batch ->
                batch.set( currentUserRef, hashMapOf("following" to state), SetOptions.merge())
                batch.set( profilerRef, hashMapOf("follower" to state), SetOptions.merge())
            }
                userInfo?.user_id?.let {
                    roomDBViewModel.apply {
                    updateFollowState(it, setState(state))
                    updateFollowCounts(it, setCount(state))
                } }

        }
    }
    private fun setState(state : Boolean) : Int{
        return if(state) 1  else 0
    }

    private fun setCount(state : Boolean) : Int{
        return if(state) 1  else -1
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<PostModel>() {

            override fun areItemsTheSame(oldConcert: PostModel,
                                         newConcert: PostModel
            ) = oldConcert.content_id == newConcert.content_id

            override fun areContentsTheSame(oldConcert: PostModel,
                                            newConcert: PostModel
            ) = oldConcert == newConcert
        }
    }


    inner class PostsListAdapter(val onPostItemsClick : OnPostItemsClickListener, val userInfoModel: UserInfoModel) :  PagingDataAdapter<PostModel, PostsListAdapter.PostsViewHolder>(DIFF_CALLBACK) {

        inner class PostsViewHolder(private val contentsAdapterBinding: ContentsAdapterBinding)
            : RecyclerView.ViewHolder(contentsAdapterBinding.root),
            View.OnClickListener {

            fun bind(postModel: PostModel?) {

                contentsAdapterBinding.apply {

                    Glide.with(itemView)
                        .load(postModel?.image_url)
                        .into(image)
                }
                itemView.setOnClickListener(this)
            }
            override fun onClick(p0: View?) {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION) {
                    getItem(position)?.let {
                        onPostItemsClick.onItemClick(R.integer.post, it, userInfoModel)
                    }
                }
            }
        }

        override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
            val binding = ContentsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return PostsViewHolder(binding)
        }
    }

    override fun onItemClick(
        navigateTO: Int,
        postModel: PostModel?,
        userInfoModel: UserInfoModel?
    ) {
        onFragmentChangeListener.onChange(this, navigateTO, Bundle()
            .apply { putParcelable("post",postModel)
                putParcelable("user info", userInfo)}) }


}