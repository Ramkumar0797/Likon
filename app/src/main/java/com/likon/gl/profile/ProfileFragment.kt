package com.likon.gl.profile

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.likon.gl.*
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.databinding.ContentsAdapterBinding
import com.likon.gl.databinding.FragmentProfileBinding
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnPostItemsClickListener
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.sealedClasses.Data
import com.likon.gl.viewModels.ProfileViewModel
import kotlinx.coroutines.flow.collectLatest
import java.lang.NullPointerException


class ProfileFragment(private val onFragmentChangeListener: OnFragmentChangeListener)
    : Fragment(R.layout.fragment_profile), View.OnClickListener, OnPostItemsClickListener,
    SwipeRefreshLayout.OnRefreshListener {


    private var _binding: FragmentProfileBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var  userInfo : UserInfoModel? = null
    private lateinit var tUsername : String
    private  var sharedPref : SharedPreferences? = null
    private lateinit var postsListAdapter: PostsListAdapter
    private lateinit var mActivity: Activity
   private lateinit var fireStore : FirebaseFirestore
    private lateinit var roomDB : RoomDBRepository
    private val viewModel : ProfileViewModel by viewModels{  ViewModelFactory(fireStore, roomDB, currentUId) }
    private lateinit var currentUId : String


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            val myApplication =  (mActivity.application as MyApplication)
            fireStore = myApplication.fireStoreDB
            roomDB = myApplication.repository
            currentUId = context.currentUserId
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        db.enableNetwork()
        _binding = FragmentProfileBinding.bind(view)

        binding.apply {

            sharedPref = activity?.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
            tUsername = sharedPref?.getString(getString(R.string.username_key),"").toString()
            username.text = tUsername

            val action = {  views : Array<View> -> for(v in views){ v.setOnClickListener(this@ProfileFragment) } }
            multiSetOnClick(arrayOf(profileMenu, textFollowers, textFollowings, textPost, countFollowers, countFollowings,
                countPosts, iconicStatus), action)

            refresher.setOnRefreshListener(this@ProfileFragment)

        }

        init()
    }

    private inline fun multiSetOnClick(  views : Array<View>, action :  (Array<View>) -> Unit){
        action(views)
    }

    private fun init(){
        binding.apply {
            infoLoader.visibility = View.VISIBLE
            profileLayout.visibility = View.INVISIBLE
        }


        lifecycleScope.launchWhenResumed {

            viewModel.getUserInfo().collectLatest {

                when (it) {
                    is Data.Success -> {
                        userInfo = it.userInfoModel
                        userInfo?.let { it1 -> setUserInfo(it1, currentUId) }
                    }
                    is Data.Loading -> {

                    }
                    else -> {

                    }
                }
            }
        }

    }

    private fun setUserInfo(userInfo : UserInfoModel, id : String){

        binding.apply {

            postsListAdapter = PostsListAdapter(this@ProfileFragment, userInfo)
            countFollowers.text = userInfo.followers.toString()
            countFollowings.text = userInfo.following.toString()
            countPosts.text = userInfo.posts.toString()


            if(username.length() == 0){
                username.text = userInfo.username
                with(sharedPref?.edit()){
                    this?.putString(getString(R.string.username_key), userInfo.username)
                    this?.apply()
                }
            }

            infoLoader.visibility = View.GONE
            profileLayout.visibility = View.VISIBLE

            fulName.text = userInfo.ful_name

            val getImage = {gender : String?, profile : String? -> profile ?: if(gender == "male")
                R.raw.male else R.raw.female
            }
            if(userInfo.profile_image == null){
                profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            Glide.with(this@ProfileFragment)
                .load( getImage(userInfo.gender, userInfo.profile_image))
                .into(profileImage)


            if(userInfo.iconic_status != null){
                iconicStatus.setImageResource(getIconicImage(userInfo.iconic_status, userInfo.gender))
            }

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

            lifecycleScope.launchWhenResumed {
                viewModel.mainFeeds(id)
                    .collectLatest { value ->
                        postsListAdapter.submitData(value)
                    }
            }

        }

    }

    override fun onClick(p0: View?) {

        binding.apply {
            when(p0?.id){

                countFollowers.id ,textFollowers.id  -> toSendData(R.integer.follow)
                countFollowings.id,textFollowings.id -> toSendData(R.integer.follow)
                countPosts.id, textPost.id           -> { }
                iconicStatus.id                          -> toSendData(R.integer.iconic_status)
                profileMenu.id                       -> onFragmentChangeListener
                                                    .onChange(this@ProfileFragment,R.integer.profile_menu,
                                                        Bundle().apply { putString("username", binding.username.text.toString())})

            }
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

    private fun toSendData(addFragment : Int){
        onFragmentChangeListener.onChange(this, addFragment, Bundle().apply { putParcelable("user info", userInfo)})
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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


    inner class PostsListAdapter(val onPostItemsClick : OnPostItemsClickListener, val userInfoModel: UserInfoModel?)
        :  PagingDataAdapter<PostModel, PostsListAdapter.PostsViewHolder>(DIFF_CALLBACK) {

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
            .apply { putParcelable("post", postModel)
                putParcelable("user info", userInfo)})
    }

    override fun onRefresh() {
        init()
        binding.refresher.isRefreshing = false
    }


}