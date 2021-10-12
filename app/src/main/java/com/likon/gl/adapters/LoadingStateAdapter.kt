package com.likon.gl.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.likon.gl.databinding.LoadStateAdapterBinding


private const val TAG = "LoadingStateAdapter"
class LoadingStateAdapter(private val retry: () -> Unit ) : LoadStateAdapter<LoadingStateAdapter.LoaderViewHolder>() {


    inner class LoaderViewHolder(private val loaderBinding: LoadStateAdapterBinding) : RecyclerView.ViewHolder(loaderBinding.root){

        init {
            loaderBinding.retry.setOnClickListener {
                retry.invoke()
            }
        }

        fun bind(loadState: LoadState){


            loaderBinding.apply {
                loader.isVisible = loadState is LoadState.Loading

                if(loadState is LoadState.Error  ){
                    Log.d(TAG, "bind: 2222222222222222222222222222222222222222222222222222222222222222222")
                    retry.isVisible = loadState.error !is ArrayIndexOutOfBoundsException
                }else{
                    Log.d(TAG, "bind: 1111111111111111111111111111111111111111111111111111111111111111111111")
                }

            }

        }

    }


    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderViewHolder {
        val binding = LoadStateAdapterBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return LoaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoaderViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

}