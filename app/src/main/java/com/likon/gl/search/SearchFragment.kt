package com.likon.gl.search

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.*
import com.likon.gl.MainActivity
import com.likon.gl.MyApplication
import com.likon.gl.R
import com.likon.gl.RoomDBViewModelFactory
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.adapters.PeopleListAdapter
import com.likon.gl.databinding.FragmentSearchBinding
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnPeopleProfileClickListener
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.RoomDBViewModel
import com.likon.gl.viewModel.SearchListViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import java.util.*

private const val TAG = "SearchFragment"


class SearchFragment(private val onFragmentChangeListener: OnFragmentChangeListener, private val onBackPressed : OnFragmentBackPressed) : Fragment(
    R.layout.fragment_search), OnPeopleProfileClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val viewModel by viewModels<SearchListViewModel>()
    private var searchJob: Job? = null
    private lateinit var mAdapter : PeopleListAdapter
    private lateinit var query : String
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((mContext.application as MyApplication).repository) }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
        }
    }

    override fun onResume() {
        super.onResume()
        mActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSearchBinding.bind(view)
        binding.apply {

          mAdapter  = PeopleListAdapter(this@SearchFragment)
            backArrow.setOnClickListener{
                hideKeyBord()
                onBackPressed.onBackPress()
            }
            peopleListView.apply {
                setHasFixedSize(true)
                adapter = mAdapter.withLoadStateFooter(footer = LoadingStateAdapter { mAdapter.retry() })
            }


            editQuery.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    query = p0.toString().trim()

                    if (  query != ""){
                        Log.d(
                            TAG,
                            "afterTextChanged: iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii $query"
                        )
                        textQuery.text = "Searching for ''$query''..."
                        search(query)
                    }else{
                        peopleListView.isVisible = false
                        startLoaderLayout.isVisible = false
                    }

                }

            })

            mAdapter.addLoadStateListener {

                if(editQuery.text.toString() != ""){

                    val loading = it.source.refresh is LoadState.Loading
                    val error = it.source.refresh is LoadState.Error
                    val errorState = it.source.append as? LoadState.Error
                            ?: it.source.prepend as? LoadState.Error
                            ?: it.append as? LoadState.Error
                            ?: it.prepend as? LoadState.Error
                            ?: it.source.refresh as? LoadState.Error
                            ?: it.refresh as? LoadState.Error

                    if(errorState?.error is ArrayIndexOutOfBoundsException&& error){
                        retry.isVisible = false
                        textQuery.text = "No result found for ''$query''"
                    }
                    startLoaderLayout.isVisible = loading || error
                        loader.isVisible = loading
                        peopleListView.isVisible = it.source.refresh is LoadState.NotLoading

                }else{
                    peopleListView.isVisible = false
                    startLoaderLayout.isVisible = false
                }

            }

            showKeyBord()
        }

    }

    private fun search(query: String) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        Log.d(TAG, "load: aaaaaaaaaaaaaaaaaaaaaaaa $query")
        searchJob = lifecycleScope.launchWhenResumed {
            viewModel.searchRepo(query, roomDBViewModel).collectLatest {
                mAdapter.submitData(it)
            }
        }
    }

    fun Job.status(): String = when {
        isActive -> "Active/Completing"
        isCompleted && isCancelled -> "Cancelled"
        isCancelled -> "Cancelling"
        isCompleted -> "Completed"
        else -> "New"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(userInfoModel: UserInfoModel?) {
        hideKeyBord()
        onFragmentChangeListener.onChange(this, R.integer.people_profile,
            Bundle().apply { putParcelable("user info", userInfoModel) })

    }

    private fun hideKeyBord(){

        binding.editQuery.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.windowInsetsController?.hide(WindowInsets.Type.ime())
            } else {
                if(this.isFocused){
                    val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(this.windowToken ,0)
                }
            }
        }
    }

    private fun showKeyBord(){

        binding.editQuery.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.windowInsetsController?.show(WindowInsets.Type.ime())
            } else {
                if (this.requestFocus()) {
                    val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }

}