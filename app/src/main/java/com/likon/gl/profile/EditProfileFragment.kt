package com.likon.gl.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserInfo
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.databinding.FragmentEditProfileBinding
import com.likon.gl.databinding.FragmentProfileMenuBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.models.SpecialPosts
import com.likon.gl.models.UserInfoModel
import com.likon.gl.share.ShareActivity

class EditProfileFragment(private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_edit_profile) {

    private var _binding: FragmentEditProfileBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private lateinit var main: MainActivity

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if ( context is MainActivity) {
            context.also {
                main = it
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentEditProfileBinding.bind(view)
        db.collection("users").document(auth.uid!!).get(Source.SERVER)
            .addOnSuccessListener { doc ->
                val userInfo = doc.toObject(UserInfoModel::class.java)
                lifecycleScope.launchWhenResumed {
                    init(userInfo)
                }
            }

    }
    private fun init(userInfo: UserInfoModel?){
        binding.apply {
            userInfo?.let {
                val getImage = { gender: String?, profile: String? ->
                    profile ?: if (gender == "male")
                        R.raw.male else R.raw.female
                }
                if (it.profile_image == null) {
                    profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
                } else {
                    profileImage.scaleType = ImageView.ScaleType.CENTER_CROP
                }

                Glide.with(this@EditProfileFragment)
                    .load(getImage(it.gender, it.profile_image))
                    .into(profileImage)

                username.setText(it.username)
                fulName.setText(it.ful_name)
                if (it.gender == "male") {
                    radioMale.isChecked = true
                    setIconicImage("male")
                } else {
                    radioFemale.isChecked = true
                    setIconicImage("female")
                }

                when (it.iconic_status) {
                    "single" -> radioSingle.isChecked = true
                    "valentine" -> radioValentine.isChecked = true
                    "married" -> radioMarried.isChecked = true
                }

                radioMale.setOnClickListener {
                    if (radioMale.isChecked) {
                        setIconicImage("male")
                    }
                }

                radioFemale.setOnClickListener {
                    if (radioFemale.isChecked) {
                        setIconicImage("female")
                    }
                }

                changeProfile.setOnClickListener {
                    val intent = Intent(main, ShareActivity::class.java)
                    intent.putExtra("type", "profile")
                    main.resultLauncher.launch(intent)
                }

                backArrow.setOnClickListener {
                    onBackPressed.onBackPress()
                }



                loader.isVisible = false
                infoLay.isVisible = true
            }
        }

    }

    private fun setIconicImage(gender : String?){

        binding.radioSingle.setCompoundDrawablesWithIntrinsicBounds(0, 0,
            when(gender){
                "male" -> R.mipmap.ic_msingle

                "female" -> R.mipmap.ic_fsingle

                else -> 0

            }, 0)


    }

}