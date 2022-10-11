package com.likon.gl.chat

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.likon.gl.*
import com.likon.gl.databinding.ChatAdapterBinding
import com.likon.gl.databinding.FragmentChatBinding
import com.likon.gl.interfaces.OnChatClickedListener
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.UserInfoModel
import com.likon.gl.models.UsersEntity
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModels.ChatsViewModel
import kotlinx.coroutines.flow.collectLatest


class ChatFragment(private val onFragmentChangeListener: OnFragmentChangeListener) : Fragment(R.layout.fragment_chat), OnChatClickedListener {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var mActivity: Activity
    private lateinit var roomDB : RoomDBRepository
    private val viewModel : ChatsViewModel
         by viewModels{  ViewModelFactory(null, roomDB, null) }
    private val mAuth = FirebaseAuth.getInstance()
    lateinit var currentUserId : String


    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            val myApplication =  (mActivity.application as MyApplication)
            roomDB = myApplication.repository
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBinding.bind(view)

        binding.apply {

            val currentUser = mAuth.currentUser

            if(currentUser != null){
                currentUserId = currentUser.uid
            }

            val adapter = ChatAdapter(this@ChatFragment)
            binding.users.adapter = adapter

            lifecycleScope.launchWhenResumed {

                viewModel.getChatFlow().collectLatest {

                    noFound.isVisible = it.isEmpty()
                    adapter.submitList(it)

                }
            }


        }
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<UsersEntity>() {

            override fun areItemsTheSame(oldConcert: UsersEntity,
                                         newConcert: UsersEntity
            ) = oldConcert.user_id == newConcert.user_id

            override fun areContentsTheSame(oldConcert: UsersEntity,
                                            newConcert: UsersEntity
            ) = oldConcert == newConcert
        }
    }

    inner  class ChatAdapter(val onChatClickedListener: OnChatClickedListener) :
        ListAdapter<UsersEntity, ChatAdapter.ChatViewHolder>(DIFF_CALLBACK){

        inner class ChatViewHolder(private val chatAdapterBinding: ChatAdapterBinding) :
            RecyclerView.ViewHolder(chatAdapterBinding.root), View.OnClickListener{
            fun bind(item: UsersEntity?) {

                chatAdapterBinding.apply {

                    item?.let {
                        username.text = it.username

                        val getImage = {gender : String?, profile : String? ->
                            profile ?: if(gender == "male") R.raw.male else R.raw.female
                        }

                        if(it.profile_image == null){
                            profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                        }
                        Glide.with(itemView)
                            .load(getImage(it.gender, it.profile_image))
                            .into(profileImage)
                        lastMessage.apply {
                            text = it.last_mge

                            Log.d("TAG", "bind: zzzzzzzzzzz ${it.sender}")
                            if(it.sender == currentUserId){
                                setCompoundDrawablesWithIntrinsicBounds(when(it.view_State){
                                    "seen" -> R.drawable.ic_baseline_done_all_24
                                    "received" -> R.drawable.ic_baseline_done_all_light_24
                                    "date" -> R.drawable.ic_baseline_done_24
                                    else -> R.drawable.ic_baseline_more_horiz_24
                                }, 0, 0, 0)
                            }else{
                                setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                            }

                        }
                        messCount.isVisible = it.unseen_count > 0
                        if (messCount.isVisible)
                            messCount.text = it.unseen_count.toString()

                    }



                }
                itemView.setOnClickListener(this)

            }

            override fun onClick(p0: View?) {
                val position = bindingAdapterPosition
                        if(position != RecyclerView.NO_POSITION){
                            getItem(position)?.let {
                                onChatClickedListener.onItemClick(UserInfoModel(it.username, it.user_id, null,
                                it.gender, null, null, 0,0, null, 0, null,
                                it.profile_image))

                            }

                        }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
            val binding = ChatAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ChatViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
            holder.bind(getItem(position))
        }

    }

    override fun onItemClick(userInfo: UserInfoModel) {
        onFragmentChangeListener.onChange(this, R.integer.message, Bundle().apply { putParcelable("user info", userInfo) })

    }

}