package com.likon.gl.common

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.likon.gl.R
import com.likon.gl.adapters.LoadingStateAdapter
import com.likon.gl.databinding.FragmentVotesBinding
import com.likon.gl.databinding.PeopleListAdapterBinding
import com.likon.gl.interfaces.*
import com.likon.gl.models.UserInfoModel
import com.likon.gl.viewModel.VoteListViewModel

import kotlinx.coroutines.flow.collectLatest
import java.lang.NullPointerException

private const val TAG = "VotesFragment"

class VotesFragment(private val onFragmentChange: OnFragmentChangeListener,
                    private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_votes), OnPeopleProfileClickListener {

    private var _binding: FragmentVotesBinding? = null
    private val binding get() = _binding!!
    private var userId : String? = null
    private var postId : String? = null
    private val votesAdapter = VotesAdapter(this)
    private val viewModel by viewModels<VoteListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getStringArray("votesInfo").apply {

            userId = this?.get(0)
            postId = this?.get(1)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentVotesBinding.bind(view)
        binding.apply {

            voteList.apply {
                setHasFixedSize(true)
                adapter = votesAdapter.withLoadStateFooter(footer = LoadingStateAdapter{votesAdapter.retry()})

            }


            votesAdapter.addLoadStateListener {
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
                voteList.isVisible = it.source.refresh is LoadState.NotLoading && !error

            }

            lifecycleScope.launchWhenResumed {


                userId?.let { uID ->
                    postId?.let { pID ->


                        viewModel.getVotes(uID, pID).collectLatest {
                            votesAdapter.submitData(it)

                        }


                    }
                }
            }

        }

    }


    companion object {
        private val DIFF_CALLBACK = object :
                DiffUtil.ItemCallback<UserInfoModel>() {

            override fun areItemsTheSame(oldConcert: UserInfoModel,
                                         newConcert: UserInfoModel
            ) = oldConcert.user_id == newConcert.user_id

            override fun areContentsTheSame(oldConcert: UserInfoModel,
                                            newConcert: UserInfoModel
            ) = oldConcert == newConcert
        }
    }

    inner class VotesAdapter(private val onPeopleProfileClickListener: OnPeopleProfileClickListener) : PagingDataAdapter<UserInfoModel, VotesAdapter.FeedsViewHolder>(DIFF_CALLBACK) {

        inner class FeedsViewHolder(private val peopleListAdapterBinding: PeopleListAdapterBinding) : RecyclerView.ViewHolder(peopleListAdapterBinding.root),
                View.OnClickListener {

            fun bind(userInfoModel: UserInfoModel?) {

                peopleListAdapterBinding.apply {

                    userInfoModel?.let {
                        username.text = userInfoModel.username
                        val getImage = {gender : String? -> if(gender == "male") R.raw.male else R.raw.female }

                        Glide.with(itemView)
                            .load(getImage(it.gender))
                            .into(profileImage)

                        name.text = userInfoModel.ful_name
                        it.iconic_status?.let { status ->
                            iconicStatus.setImageResource(getIconicImage(status, it.gender) )
                        }

                    }


                }
            }

            init {
                itemView.setOnClickListener(this)
                peopleListAdapterBinding.apply {

                }
            }

            override fun onClick(p0: View?) {
                val position = bindingAdapterPosition
                if(position != RecyclerView.NO_POSITION){

                        onPeopleProfileClickListener.onItemClick(getItem(position))
                        peopleListAdapterBinding.apply {
                            when(p0?.id){


                            }
                        }

                }
            }
        }

        override fun onBindViewHolder(holder: FeedsViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedsViewHolder {
            val binding = PeopleListAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return FeedsViewHolder(binding)
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

    override fun onItemClick(userInfoModel: UserInfoModel?) {
        onFragmentChange.onChange(this, R.integer.people_profile,
            Bundle().apply { putParcelable("user info", userInfoModel) })
    }

}