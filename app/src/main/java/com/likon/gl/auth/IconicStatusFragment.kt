package com.likon.gl.auth

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.likon.gl.R
import com.likon.gl.databinding.FragmentIconicStatusBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener


class IconicStatusFragment : Fragment(R.layout.fragment_iconic_status), View.OnClickListener {

    private var _binding: FragmentIconicStatusBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener: OnUserDataSetListener
    private lateinit var iconicStatus : String
    private lateinit var gender : String
    private lateinit var activity : Context
    private lateinit var titleChangeListener: TitleChangeListener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is NewUserActivity){
            activity = context
            context.also { onUserDataSetListener = it
                titleChangeListener = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        titleChangeListener.onChange("Iconic Status")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iconicStatus = arguments?.getString("iconic status") ?: ""
        gender = arguments?.getString("gender") ?: ""
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentIconicStatusBinding.bind(view)
        binding.apply {

            when(iconicStatus){

                "single" -> radioSingle.isChecked = true
                "valentine" -> radioValentine.isChecked = true
                "married" -> radioMarried.isChecked = true

            }

            radioSingle.apply {
                setOnClickListener(this@IconicStatusFragment)
                setCompoundDrawablesWithIntrinsicBounds(0, 0, getIconicImage(gender), 0)

            }
            radioValentine.setOnClickListener(this@IconicStatusFragment)
            radioMarried.setOnClickListener(this@IconicStatusFragment)
            iconicNext.setOnClickListener(this@IconicStatusFragment)

        }

    }

    private fun getIconicImage(gender : String?): Int {


        return when(gender){
            "male" -> R.mipmap.ic_msingle

            "female" -> R.mipmap.ic_fsingle

            else -> 0


        }

    }

    override fun onClick(p0: View?) {

        if(p0?.id == binding.iconicNext.id){

            iconicNext()
            return
        }
        binding.iconicError.text = ""

    }

    private fun iconicNext() {
        if(binding.radioSingle.isChecked || binding.radioMarried.isChecked || binding.radioValentine.isChecked ){
            binding.iconicNext.isEnabled = false
            when {
                binding.radioSingle.isChecked -> {
                    onUserDataSetListener.onDataSet(this,5,"single",null)
                }
                binding.radioValentine.isChecked -> {
                    onUserDataSetListener.onDataSet(this,5,"valentine",null)
                }
                binding.radioMarried.isChecked -> {
                    onUserDataSetListener.onDataSet(this,5,"married",null)
                }

            }

        }else{
            binding.iconicError.text = "You must choose any one above!"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}