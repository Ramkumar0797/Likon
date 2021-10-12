package com.likon.gl.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.likon.gl.R
import com.likon.gl.databinding.FragmentGenderBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener

class GenderFragment : Fragment(R.layout.fragment_gender), View.OnClickListener {

    private var _binding: FragmentGenderBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener: OnUserDataSetListener
    private lateinit var gender : String
    private lateinit var titleChangeListener: TitleChangeListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gender = arguments?.getString("gender") ?: ""
    }

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
        titleChangeListener.onChange("Gender")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentGenderBinding.bind(view)
        binding.apply {
            if(gender == "female" ){
                radioFemale.isChecked = true
            }else if(gender == "male"){
                radioMale.isChecked = true
            }
            genderNext.setOnClickListener(this@GenderFragment)
            radioFemale.setOnClickListener(this@GenderFragment)
            radioMale.setOnClickListener(this@GenderFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(p0: View?) {

        if(p0?.id == binding.genderNext.id){
            genderNext()
            return
        }
        binding.genderError.text = ""

    }

    private fun genderNext() {
        if(binding.radioFemale.isChecked || binding.radioMale.isChecked){
            binding.genderNext.isEnabled = false
            if(binding.radioFemale.isChecked){
                onUserDataSetListener.onDataSet(this,3,"female",null)
            }else if(binding.radioMale.isChecked){
                onUserDataSetListener.onDataSet(this,3,"male",null)
            }

        }else{
            binding.genderError.text = "You must choose any one above!"
        }
    }

}