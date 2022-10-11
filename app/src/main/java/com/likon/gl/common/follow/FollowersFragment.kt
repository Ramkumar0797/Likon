package com.likon.gl.common.follow


import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.likon.gl.*
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.adapters.PeopleListAdapter
import com.likon.gl.databinding.FragmentFollowersBinding
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnPeopleProfileClickListener
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModels.FollowListViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.NullPointerException


class FollowersFragment(private val containerFragment : Fragment, private val onFragmentChangeListener: OnFragmentChangeListener) : Fragment(
    R.layout.fragment_followers), OnPeopleProfileClickListener {

    private var _binding: FragmentFollowersBinding? = null
    private val binding get() = _binding!!
    private var userInfoModel : UserInfoModel? = null
    private val peopleListAdapter = PeopleListAdapter(this)
    private lateinit var mActivity: Activity
    private lateinit var roomDB : RoomDBRepository
    private lateinit var fireStore : FirebaseFirestore


    private val viewModel : FollowListViewModel by
    viewModels{ ViewModelFactory(fireStore, roomDB, null) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            val myApplication =  (mActivity.application as MyApplication)
            roomDB = myApplication.repository
            fireStore = myApplication.fireStoreDB

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            arguments?.let {
                userInfoModel = it.getParcelable("user info")
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFollowersBinding.bind(view)

        binding.apply {
            followersContainer.apply {
                setHasFixedSize(true)
                adapter = peopleListAdapter.withLoadStateFooter(footer = LoadingStateAdapter{peopleListAdapter.retry()})
                getData()
            }
            retry.setOnClickListener {
                peopleListAdapter.retry()
            }

            followersRefresher.setOnRefreshListener {

                peopleListAdapter.refresh()
                followersRefresher.isRefreshing = false
            }

            peopleListAdapter.addLoadStateListener {
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
                followersRefresher.isVisible = it.source.refresh is LoadState.NotLoading && !error

            }

        }
    }


        private fun getData(){

            lifecycleScope.launch {

                viewModel.followList(userInfoModel?.user_id!!,"follower").collectLatest {
                    peopleListAdapter.submitData(it)

                }

            }

        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(userInfoModel: UserInfoModel?) {
        onFragmentChangeListener.onChange(containerFragment,R.integer.people_profile,Bundle().apply { putParcelable("user info", userInfoModel) })
    }

//    companion object {
//        private val DIFF_CALLBACK = object :
//                DiffUtil.ItemCallback<UserInfoModel>() {
//
//            override fun areItemsTheSame(oldConcert: UserInfoModel,
//                                         newConcert: UserInfoModel
//            ) = oldConcert.user_id == newConcert.user_id
//
//            override fun areContentsTheSame(oldConcert: UserInfoModel,
//                                            newConcert: UserInfoModel
//            ) = oldConcert == newConcert
//        }
//    }


//    inner class FollowListAdapter(private val onPeopleItemClickListener: OnPeopleItemClickListener) : PagingDataAdapter<UserInfoModel, FollowListAdapter.PeopleViewHolder>(DIFF_CALLBACK) {
//
//
//        inner class PeopleViewHolder(private val peopleListAdapterBinding: PeopleListAdapterBinding) : RecyclerView.ViewHolder(peopleListAdapterBinding.root),
//                View.OnClickListener {
//
//            fun bind(usersInfo : UserInfoModel?) {
//
//                peopleListAdapterBinding.apply {
////                Glide.with(context)
////                        .load(usersInfo.)
////                        .into(firstImage)
//
//                    username.text = usersInfo?.username
////                    set(usersInfo?.username, username)
//                }
//            }
//
//            init {
//                itemView.setOnClickListener(this)
//            }
//
//            override fun onClick(p0: View?) {
//                val position = bindingAdapterPosition
//                if(position != RecyclerView.NO_POSITION){
//                    getItem(position)?.let {
//                        onPeopleItemClickListener.onItemClick(it)
//                    }
//                }
//            }
//
//        }
//
//        override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
//            holder.bind(getItem(position))
//
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
//            val binding = PeopleListAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//            return PeopleViewHolder(binding)
//        }
//
//    }




}