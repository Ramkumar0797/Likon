package com.likon.gl.auth

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.likon.gl.MainActivity
import com.likon.gl.databinding.ActivityNewUserBinding
import com.likon.gl.interfaces.OnUserDataSetListener
import com.likon.gl.interfaces.TitleChangeListener
import com.likon.gl.R
import com.likon.gl.models.SpecialPosts
import java.util.*




class NewUserActivity : AppCompatActivity() , OnUserDataSetListener, TitleChangeListener {

    private lateinit var mTitle :TextView
    private  var name : String? = null
    private var gender : String? = null
    private var iconicStatus : String? = null
    private var dob : Date? = null
    private var spd : Date? = null
    private  var mailName : String? = null
    private  var username : String? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityNewUserBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {
            mTitle = title.apply { text = "Welcome" }
            backArrow.setOnClickListener { onBackPressed() }
            mailName = GoogleSignIn.getLastSignedInAccount(this@NewUserActivity)?.displayName
            supportFragmentManager.apply {
                beginTransaction().apply { add(R.id.new_user_container,WelcomeFragment().apply { arguments = Bundle().apply { putString("name",mailName) } })
                    setReorderingAllowed(true)
                    commit() }
            }

        }
    }

    override fun onDataSet(currentFragment: Fragment, addFragment: Int, value: String?, date: Date?) {
        when(addFragment){
            1 -> onTransaction(FulNameFragment().apply { arguments = Bundle().apply { putStringArray("name",
                arrayOf(name, mailName)) } })

            2 -> {name = value
                onTransaction(GenderFragment().apply { arguments = Bundle().apply { putString("gender",gender) } })}

            3 -> {gender = value
                onTransaction(DOBFragment().apply { arguments = Bundle().apply { dob?.let { putLong("dob", it.time) } } })}

            4 -> {dob = date
                onTransaction(IconicStatusFragment().apply { arguments = Bundle().apply { putString("iconic status",iconicStatus)
                putString("gender", gender)} })}

            5 -> {iconicStatus = value
                onTransaction(SpecialPostFragment().apply { arguments = Bundle().apply { putString("iconic status",iconicStatus)
                    dob?.let { putLong("dob", it.time) }} })}

            6 -> {spd = date
                onTransaction(UsernameFragment())}

            7 -> {username = value
                toMainActivity(currentFragment)}
        }
    }

    private fun onTransaction(addFragment: Fragment){
        supportFragmentManager.apply {
            beginTransaction().apply {
                setCustomAnimations(R.anim.visible_from_end, 0,R.anim.visible_from_start,0)
                replace(R.id.new_user_container,addFragment)
                setReorderingAllowed(true)
                addToBackStack("")
                commit()

            }
        }
    }

    private fun toMainActivity(currentFragment : Fragment){
            auth.uid?.let {
                val fragment = currentFragment as UsernameFragment
                val userInfo = hashMapOf("username" to username,
                    "user_id" to it,
                    "ful_name" to name,
                    "gender" to gender,
                    "iconic_status" to iconicStatus,
                    "date_of_birth" to dob,
                    "followers" to 0,
                    "following" to 0,
                    "profile_image" to null,
                    "posts" to 0,
                    "registered_date" to FieldValue.serverTimestamp(),
                    "spd" to spd )

                val spdInfo = SpecialPosts(dob, spd, iconicStatus, null, null)

                db.runTransaction { transaction ->
                    transaction.set(db.collection("users").document(it), userInfo, SetOptions.merge())

                    transaction.set(db.collection("users").document(it)
                        .collection("special_posts").document("posts_info"), spdInfo)

                }.addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }.addOnFailureListener {
                    fragment.toSetError()
                }
            }
    }

    override fun onChange(title : String) {

        mTitle.text = title
    }

    override fun onBackPressed() {
        if(binding.title.text == "Welcome" || binding.title.text == "Name" || binding.title.text == "Gender") {
            super.onBackPressed()
        } else{
            AlertDialog.Builder(this@NewUserActivity).apply {
                setTitle("Warning")
                setMessage("If you get back, saved data may removed. Are sure want to get back?")
                setPositiveButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                setNegativeButton("Yes") { dialog, _ ->
                    super.onBackPressed()

                    dialog.dismiss()
                }

            }.show()
        }
    }


}