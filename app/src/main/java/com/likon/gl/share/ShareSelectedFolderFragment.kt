package com.likon.gl.share

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.databinding.ContentsAdapterBinding
import com.likon.gl.databinding.FragmentShareSelectedFolderBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnItemClickedListener
import com.likon.gl.models.ImageFoldersModel


class ShareSelectedFolderFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                                  private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_share_selected_folder), OnItemClickedListener {

    private var _binding: FragmentShareSelectedFolderBinding? = null
    private val binding get() = _binding!!
    private  var imageFoldersModel : ImageFoldersModel?= null
    private lateinit var images: List<Uri>
    private lateinit var mActivity: Activity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is ShareActivity){
            mActivity = context
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            imageFoldersModel = it.getParcelable("selected folder")
            imageFoldersModel?.let {it1->
                images = it1.imagesUris.reversed()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { super.onViewCreated(view, savedInstanceState)
        _binding = FragmentShareSelectedFolderBinding.bind(view)

        binding.apply {
            backArrow.setOnClickListener { onBackPressed.onBackPress() }
            val mAdapter =  ContentsAdapter(images, this@ShareSelectedFolderFragment)
            container.adapter = mAdapter
            title.text = imageFoldersModel?.folderName

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    class ContentsAdapter(  images: List<Uri>, private val onItemClickable: OnItemClickedListener) :
        RecyclerView.Adapter<ContentsAdapter.ContentsViewHolder>() {

        private val mImages: List<Uri> = images

        inner class  ContentsViewHolder(private val contentsAdapterBinding: ContentsAdapterBinding) :
            RecyclerView.ViewHolder(contentsAdapterBinding.root), View.OnClickListener {
            fun bind(imageUri: Uri){

                contentsAdapterBinding.apply {
                    Glide.with(itemView)
                            .load(imageUri)
                            .into(image)

                }
            }

            init {
                itemView.setOnClickListener(this)
            }

            override fun onClick(p0: View?) {
                onItemClickable.onItemClick(bindingAdapterPosition)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContentsViewHolder {
            val binding = ContentsAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ContentsViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ContentsViewHolder, position: Int) {
            holder.bind(mImages[position])

        }

        override fun getItemCount(): Int {
            return mImages.size
        }

    }

    override fun onItemClick(position: Int) {

        onFragmentChangeListener.onChange(this, R.integer.share,
            Bundle().apply { putString("content", images[position].toString() ) })
    }


}