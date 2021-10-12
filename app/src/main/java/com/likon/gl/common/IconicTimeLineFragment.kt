package com.likon.gl.common

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.R
import com.likon.gl.databinding.FragmentIconicStatusBinding
import com.likon.gl.databinding.FragmentIconicTimeLineBinding
import com.likon.gl.models.UserInfoModel


class IconicTimeLineFragment : Fragment(R.layout.fragment_iconic_time_line) {

    private var _binding: FragmentIconicTimeLineBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var  userInfo : UserInfoModel? = null
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIconicTimeLineBinding.bind(view)

    }

}