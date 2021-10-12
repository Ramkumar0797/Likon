package com.likon.gl.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.likon.gl.R
import com.likon.gl.databinding.FragmentWelcomeBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener


class WelcomeFragment  : Fragment(R.layout.fragment_welcome) {

    private var _binding: FragmentWelcomeBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener: OnUserDataSetListener
    private lateinit var titleChangeListener: TitleChangeListener
    private lateinit var name : String

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is NewUserActivity){
            context.also { onUserDataSetListener = it
            titleChangeListener = it
            }
        }

    }

    override fun onResume() {
        super.onResume()
        titleChangeListener.onChange("Welcome")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        name = arguments?.getString("name") ?: ""

    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWelcomeBinding.bind(view)
        binding.apply {

            fullName.text = name
            welcomeNext.setOnClickListener {
                it.isEnabled = false
                onUserDataSetListener.onDataSet(this@WelcomeFragment,1,null,null)
            }

        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}