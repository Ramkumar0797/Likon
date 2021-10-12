package com.likon.gl.auth

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.DatePicker
import androidx.core.view.isVisible
import com.likon.gl.R
import com.likon.gl.databinding.FragmentSpecialPostBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener
import java.util.*

class SpecialPostFragment : Fragment(R.layout.fragment_special_post) {


    private var _binding: FragmentSpecialPostBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener: OnUserDataSetListener
    private lateinit var iconicStatus : String
    private  var dob : Long = 0
    private lateinit var titleChangeListener: TitleChangeListener

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
        titleChangeListener.onChange("Special Post Day")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            iconicStatus = it.getString("iconic status") ?: ""
            dob = it.getLong("dob")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpecialPostBinding.bind(view)

            when(iconicStatus){
                "married" ->{
                    binding.datePicker.minDate = dob
                    setVisibility(married = true, valentine = false, single = false)
                }
                "valentine" ->{
                    setVisibility(married = false, valentine = true, single = false)

                }
                "single" ->{
                    setVisibility(married = false, valentine = false, single = true)

                }
            }

        binding.apply {
            SPDNext.setOnClickListener {
                when(iconicStatus){
                    "married" ->{

                        onUserDataSetListener.onDataSet(this@SpecialPostFragment, 6,null,
                            getDateFromDatePicker(binding.datePicker))
                    }
                    "valentine" ->{
                        onUserDataSetListener.onDataSet(this@SpecialPostFragment, 6,null,
                            getDate(1996, 2, 14))
                    }
                    "single" ->{
                        onUserDataSetListener.onDataSet(this@SpecialPostFragment, 6, null,
                            getDate(1996, 2, 29))
                    }
                }

            }
        }

    }

    private fun setVisibility(married : Boolean, valentine : Boolean, single : Boolean){
        binding.apply {
            singleLay.isVisible = single
            valentineLay.isVisible = valentine
            marriedLay.isVisible = married
        }
    }

    private fun getDateFromDatePicker(datePicker: DatePicker): Date {
        return getDate(datePicker.year, datePicker.month, datePicker.dayOfMonth)
    }

    private fun getDate(year : Int, month : Int, day : Int) : Date{
       return Calendar.getInstance().apply { set(year, month, day, 0, 0, 0) }.time
    }

}