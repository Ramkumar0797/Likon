package com.likon.gl.home
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likon.gl.*
import com.likon.gl.databinding.FragmentUploadingBinding
import com.likon.gl.databinding.UploadsAdapterBinding
import com.likon.gl.interfaces.OnBottomNavVisibilityListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.models.UploadEntityModel
import com.likon.gl.share.ShareActivity
import com.likon.gl.viewModels.RoomDBViewModel
import com.likon.gl.viewModels.UploadingViewModel

import kotlinx.coroutines.flow.collectLatest


class UploadingFragment( private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_uploading) {

    private var _binding: FragmentUploadingBinding? = null
    private val binding get() = _binding!!
    private lateinit var mActivity: Activity
    private lateinit var main: MainActivity
    private val mContext get() = mActivity
    private val viewModel: UploadingViewModel by viewModels { ViewModelFactory(null,
        (mContext.application as MyApplication).repository, null) }
    private val uploadAdapter = UploadAdapter()
    private lateinit var onBottomNavListener : OnBottomNavVisibilityListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if ( context is MainActivity) {
            context.also {
                mActivity = it
                onBottomNavListener  = it
                main = it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUploadingBinding.bind(view)
        onBottomNavListener.setVisibility(false)
        binding.apply {

            uploadsList.adapter = uploadAdapter
            lifecycleScope.launchWhenResumed {

                viewModel.getUploads().collectLatest {

                    noResult.isVisible = it.isEmpty()

                    uploadAdapter.submitList(it)

                }
            }
            backArrow.setOnClickListener{onBackPressed.onBackPress()}

            shareButton.setOnClickListener {
                shareButton.isEnabled = false
                val  intent = Intent(mActivity, ShareActivity::class.java)
                intent.putExtra("type","post")
                main.resultLauncher.launch(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.shareButton.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        onBottomNavListener.setVisibility(true)
    }

    class UploadAdapter : ListAdapter<UploadEntityModel, UploadAdapter.UploadViewHolder>(UploadComparator()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):  UploadViewHolder{
            val binding = UploadsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return UploadViewHolder(binding)
        }

        override fun onBindViewHolder(holder: UploadViewHolder, position: Int) {
            holder.bind(getItem(position))

        }

        inner class  UploadViewHolder(private val uploadsAdapterBinding: UploadsAdapterBinding) : RecyclerView.ViewHolder(uploadsAdapterBinding.root){

            fun bind(uploadEntityModel: UploadEntityModel) {

                uploadsAdapterBinding.apply {
                    Glide.with(itemView)
                            .load(uploadEntityModel.uri)
                            .into(image)
                    progress.progress = uploadEntityModel.progress
                }


            }
        }

        class UploadComparator : DiffUtil.ItemCallback<UploadEntityModel>() {
            override fun areItemsTheSame(oldItem: UploadEntityModel, newItem: UploadEntityModel): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: UploadEntityModel, newItem: UploadEntityModel): Boolean {
                return oldItem.post_id == newItem.post_id
            }
        }
    }


}