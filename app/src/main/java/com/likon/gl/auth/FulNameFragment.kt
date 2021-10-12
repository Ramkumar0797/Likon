package com.likon.gl.auth

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.View
import android.view.inputmethod.EditorInfo
import com.likon.gl.R
import com.likon.gl.databinding.FragmentFulNameBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener

class FulNameFragment : Fragment(R.layout.fragment_ful_name) {

    private var _binding: FragmentFulNameBinding? = null
    private val binding get() = _binding!!
    private  lateinit var onUserDataSetListener : OnUserDataSetListener
    private lateinit var name : String
    private lateinit var mailName : String
    private lateinit var titleChangeListener: TitleChangeListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.getStringArray("name").apply {
            name = this?.get(0) ?: ""
            mailName = this?.get(1) ?: ""
        }

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
        titleChangeListener.onChange("Name")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFulNameBinding.bind(view)
        binding.apply {
            if(name != ""){
                fulName.setText(name)
            }else{
                fulName.setText(mailName)
            }

            fulName.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                override fun afterTextChanged(p0: Editable?) {
                    fullNameLayout.isErrorEnabled = false
                }

            })

            nameNext.setOnClickListener{

                fulName.onEditorAction(EditorInfo.IME_ACTION_DONE)
                if(fulName.text?.length == 0){
                    fullNameLayout.error = "Should not leave as empty this field!"
                }else{
                    it.isEnabled = false
                    onUserDataSetListener.onDataSet(this@FulNameFragment,2,fulName.text.toString().trim(),null)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}