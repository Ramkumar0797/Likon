package com.likon.gl.search

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.R
import com.likon.gl.databinding.FragmentSuggestContentBinding
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.UserInfoModel


class SuggestContentFragment(private val onFragmentChangeListener: OnFragmentChangeListener) : Fragment(
    R.layout.fragment_suggest_content) {

    private var _binding: FragmentSuggestContentBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var  userInfo : UserInfoModel? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSuggestContentBinding.bind(view)
        binding.apply {
            searchBar.setOnClickListener{
                onFragmentChangeListener.onChange(this@SuggestContentFragment,R.integer.search,null)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}