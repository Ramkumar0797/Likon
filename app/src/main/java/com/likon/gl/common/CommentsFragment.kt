package com.likon.gl.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.databinding.FragmentCommentsBinding
import com.likon.gl.databinding.FragmentPostBinding
import com.likon.gl.databinding.FragmentVotesBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.UserInfoModel


class CommentsFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                       private val onBackPressed : OnFragmentBackPressed
) : Fragment(R.layout.fragment_comments) {

    private var _binding: FragmentCommentsBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private lateinit var currentUId : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            currentUId = context.currentUserId
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCommentsBinding.bind(view)
        binding.apply {
            backArrow.setOnClickListener {
                onBackPressed.onBackPress()
            }

        }
    }
}