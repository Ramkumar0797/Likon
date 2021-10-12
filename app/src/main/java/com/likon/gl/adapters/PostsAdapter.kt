package com.likon.gl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likon.gl.databinding.GalleryAdapterBinding
import com.likon.gl.interfaces.OnPostItemsClickListener
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel


class PostsAdapter(val onPostItemsClick : OnPostItemsClickListener, val userInfoModel: UserInfoModel) :  PagingDataAdapter<PostModel, PostsAdapter.PostsViewHolder>(
    DIFF_CALLBACK
) {

    inner class PostsViewHolder(private val galleryAdapterBinding: GalleryAdapterBinding) : RecyclerView.ViewHolder(galleryAdapterBinding.root),
        View.OnClickListener {


        fun bind(postModel: PostModel?) {

            galleryAdapterBinding.apply {

                Glide.with(itemView)
                    .load(postModel?.image_url)
                    .into(firstImage)

            }
            itemView.setOnClickListener(this)

        }
        override fun onClick(p0: View?) {

            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION) {
                getItem(position)?.let {
                    onPostItemsClick.onItemClick(0, it, userInfoModel)
                }
            }
        }

    }

    override fun onBindViewHolder(holder: PostsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostsViewHolder {
        val binding = GalleryAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostsViewHolder(binding)
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

}