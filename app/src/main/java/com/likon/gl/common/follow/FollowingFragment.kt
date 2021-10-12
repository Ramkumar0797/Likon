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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.likon.gl.MainActivity
import com.likon.gl.MyApplication
import com.likon.gl.R
import com.likon.gl.RoomDBViewModelFactory
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.adapters.PeopleListAdapter
import com.likon.gl.databinding.FragmentFollowingBinding
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnPeopleProfileClickListener
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.FollowListViewModel
import com.likon.gl.viewModel.RoomDBViewModel

import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.lang.NullPointerException


class FollowingFragment(private val containerFragment : Fragment, private val onFragmentChangeListener: OnFragmentChangeListener) : Fragment(
    R.layout.fragment_following), OnPeopleProfileClickListener {


    private var _binding: FragmentFollowingBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var userInfoModel : UserInfoModel? = null
    private val viewModel by viewModels<FollowListViewModel>()
    private val peopleListAdapter = PeopleListAdapter(this)
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((mContext.application as MyApplication).repository) }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfoModel = it.getParcelable("user info")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFollowingBinding.bind(view)
        binding.apply {
            followingContainer.apply {
                setHasFixedSize(true)
                adapter = peopleListAdapter.withLoadStateFooter(footer = LoadingStateAdapter{peopleListAdapter.retry()})
                getData()
            }
            retry.setOnClickListener {
                peopleListAdapter.retry()
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
                followingContainer.isVisible = it.source.refresh is LoadState.NotLoading && !error
            }

        }
    }

    private fun getData(){

        lifecycleScope.launch {

            viewModel.followList(userInfoModel?.user_id!!,"following", roomDBViewModel).collectLatest {
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

}