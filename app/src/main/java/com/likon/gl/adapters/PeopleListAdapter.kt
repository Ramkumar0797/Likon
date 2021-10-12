package com.likon.gl.adapters


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.likon.gl.R
import com.likon.gl.databinding.PeopleListAdapterBinding
import com.likon.gl.interfaces.OnPeopleProfileClickListener
import com.likon.gl.models.UserInfoModel


private const val TAG = "PeopleListAdapter"
class PeopleListAdapter(private val onPeopleProfileClickListener: OnPeopleProfileClickListener) : PagingDataAdapter<UserInfoModel, PeopleListAdapter.PeopleViewHolder>(DIFF_CALLBACK) {



    inner class PeopleViewHolder(private val peopleListAdapterBinding: PeopleListAdapterBinding) : RecyclerView.ViewHolder(peopleListAdapterBinding.root),
            View.OnClickListener {

        fun bind(usersInfo : UserInfoModel?) {

            peopleListAdapterBinding.apply {

                usersInfo?.let {
                    username.text = it.username
                    name.text = it.ful_name

                    val getImage = {gender : String?, profile : String? ->
                        profile ?: if(gender == "male") R.raw.male else R.raw.female
                    }

                    if(it.profile_image == null){
                        profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                    }
                    Glide.with(itemView)
                        .load(getImage(it.gender, it.profile_image))
                        .into(profileImage)
                    it.iconic_status?.let { status ->
                            iconicStatus.setImageResource(getIconicImage(status, it.gender) )
                    }

                }



            }
        }

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val position = bindingAdapterPosition
            if(position != RecyclerView.NO_POSITION){

                   onPeopleProfileClickListener.onItemClick(getItem(position))

            }
        }

    }

    override fun onBindViewHolder(holder: PeopleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PeopleViewHolder {
        val binding = PeopleListAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PeopleViewHolder(binding)
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

    private fun getIconicImage(iconicStatus: String, gender : String?): Int {

        val getImage = {temp : String? -> if(temp == "male") R.raw.msingle else R.raw.fsingle}
        return when(iconicStatus){


            "single" -> getImage(gender)

            "valentine" -> R.raw.heart

            "married" -> R.raw.couple

            else -> 0
        }

    }

}