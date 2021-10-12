package com.likon.gl.profile

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.auth.SignInActivity
import com.likon.gl.databinding.FragmentProfileMenuBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ProfileMenuFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                          private val onBackPressed : OnFragmentBackPressed
) : Fragment(R.layout.fragment_profile_menu) {


    private var _binding: FragmentProfileMenuBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private lateinit var tUsername : String
    private lateinit var mActivity: Activity
    private lateinit var alertDialog : AlertDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            tUsername = it.getString("username").toString()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProfileMenuBinding.bind(view)
        binding.apply {

            username.text = tUsername
            specialPost.setOnClickListener {
                onFragmentChangeListener
                    .onChange(this@ProfileMenuFragment,R.integer.special_post, null)
            }

            editProfile.setOnClickListener {
                onFragmentChangeListener
                    .onChange(this@ProfileMenuFragment,R.integer.edit_profile, null)
            }

            backArrow.setOnClickListener {
                onBackPressed.onBackPress()
            }

            alertDialog = AlertDialog.Builder(requireContext()).apply {
                setView(R.layout.sign_out_progress_layout)
                setCancelable(false)
            }.create()



            signingOut.setOnClickListener {
                AlertDialog.Builder(mActivity).apply {
                    setTitle("Sign out")
                    setMessage("Are you sure to sign out?")
                    setPositiveButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    setNegativeButton("Yes") { dialog, _ ->
                        dialog.dismiss()
                        alertDialog.show()
                        FirebaseAuth.getInstance().signOut()

                        val scope = CoroutineScope(Dispatchers.Main)

                        scope.launch {
                            delay(2000)
                            mActivity.startActivity(Intent(mActivity, SignInActivity::class.java))
                            mActivity.finish()
                        }

                    }

                }.show()
            }

        }
    }
}