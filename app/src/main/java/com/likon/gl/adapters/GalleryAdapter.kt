package com.likon.gl.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likon.gl.databinding.GalleryAdapterBinding
import com.likon.gl.models.ImageFoldersModel


class GalleryAdapter(  imageFolderModels: List<ImageFoldersModel>, private val onItemClickable: OnGalleryItemClickable) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    interface OnGalleryItemClickable {
        fun onItemClick(position: Int)
    }

    private val mImageFolderModels: List<ImageFoldersModel> = imageFolderModels


    inner class  GalleryViewHolder(private val galleryAdapterBinding: GalleryAdapterBinding) : RecyclerView.ViewHolder(galleryAdapterBinding.root), View.OnClickListener {
        fun bind(foldersModel: ImageFoldersModel?){

            galleryAdapterBinding.apply {
                Glide.with(itemView)
                        .load(foldersModel?.firstPic)
                        .into(firstImage)
                folderName.text = foldersModel?.folderName ?: ""
                imageCount.text = foldersModel?.numberOfPics.toString()

            }
        }



        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            onItemClickable.onItemClick(bindingAdapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val binding = GalleryAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GalleryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        holder.bind(mImageFolderModels[position])

    }

    override fun getItemCount(): Int {
        return mImageFolderModels.size
    }

}