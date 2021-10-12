package com.likon.gl.auth

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.likon.gl.R
import com.likon.gl.databinding.FragmentDOBBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener
import java.util.*

private const val TAG = "DOBFragment"

class DOBFragment : Fragment(R.layout.fragment_d_o_b) {

    private var _binding: FragmentDOBBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener: OnUserDataSetListener
    private var dob : Long = 0
    private lateinit var alertDialog : AlertDialog
    private lateinit var titleChangeListener: TitleChangeListener
    private lateinit var functions : FirebaseFunctions

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnUserDataSetListener && context is TitleChangeListener){
            context.also { onUserDataSetListener = it
                titleChangeListener = it
            }
        }
    }

    override fun onResume() {
        super.onResume()
        titleChangeListener.onChange("Date Of Birth")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dob = it.getLong("dob")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: ssssssssssssssssssssssssssssss $dob")
        _binding = FragmentDOBBinding.bind(view)
        functions = Firebase.functions
        alertDialog = AlertDialog.Builder(requireContext()).apply {
            setView(R.layout.dialog_progress_layout)
            setCancelable(false)
        }.create()
        binding.apply {


          datePicker.apply {

                maxDate = Date().time
            }

            dobNext.setOnClickListener{
                    it.isEnabled = false
                    hideSoftKeyboard()
                    alertDialog.show()
                getDate()
            }
        }
    }

    private  fun getDate(){

        functions.getHttpsCallable("getTime")
            .call()
            .addOnCompleteListener{

                if(!it.isSuccessful){
                    showError("Network error", "Internet connection lost!")
                }else{
                    validateDate(Date(it.result.data as Long))
                }
            }

    }

    private fun validateDate( currentDate : Date){

                        val selectedDate = getDateFromDatePicker(binding.datePicker)
                        val serverDate = Calendar.getInstance().apply { time = currentDate }

                            if (selectedDate.time <= currentDate) {


                                var age: Int = serverDate.get(Calendar.YEAR) - selectedDate.get(Calendar.YEAR)
                                if (serverDate.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) {
                                    age--
                                }

                                if (age >= 16) {
                                    alertDialog.dismiss()
                                    onUserDataSetListener.onDataSet(this, 4, null, selectedDate.time)
                                }else{
                                    showError("Age restriction", "We cannot process with you entered date!")
                                }

                            }else{
                                showError("Invalid Date", "Please Choose valid date.")
                            }

    }

    private fun  showError(title: String, message: String){
        alertDialog.dismiss()
        binding.dobNext.isEnabled = true
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Ok") { dialog, _ ->
                    dialog.dismiss()
                }.show()
    }

    private fun getDateFromDatePicker(datePicker: DatePicker): Calendar {

        return  Calendar.getInstance().apply {
            set(datePicker.year, datePicker.month, datePicker.dayOfMonth, 0, 0, 0)
        }
    }

    private fun hideSoftKeyboard() {
            val inputMethodManager = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)!!
            inputMethodManager.hideSoftInputFromWindow(requireActivity().currentFocus?.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}