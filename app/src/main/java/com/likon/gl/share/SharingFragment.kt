package com.likon.gl.share

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.likon.gl.MyApplication
import com.likon.gl.R
import com.likon.gl.ViewModelFactory
import com.likon.gl.databinding.FragmentSharingBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.models.UploadEntityModel
import com.likon.gl.viewModels.RoomDBViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

private const val TAG = "SharingFragment"
class SharingFragment( private val onBackPressed : OnFragmentBackPressed) : Fragment(
    R.layout.fragment_sharing) {

    private var _binding: FragmentSharingBinding? = null
    private val binding get() = _binding!!
    private var url: String? = null
    private var type: String? = null
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel: RoomDBViewModel by
    viewModels {  ViewModelFactory(null,(mContext.application as MyApplication).repository, null) }
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private lateinit var alertDialog : AlertDialog
    private lateinit var currentUserId : String
    private val mAuth = FirebaseAuth.getInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if ( context is ShareActivity) {
            context.also {
                mActivity = it

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            url = it.getString("content")
            type = it.getString("type")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSharingBinding.bind(view)
        val currentUser = mAuth.currentUser
        alertDialog = AlertDialog.Builder(mContext).apply {
            setView(R.layout.dialog_progress_layout)
            setCancelable(false)
        }.create()
        if(currentUser != null){
            currentUserId = currentUser.uid
        }
        if(type == "post"){
            setPostInfo()
        }else if(type == "profile"){
            setProfileInfo()
        }

    }

    private  fun setProfileInfo(){
        binding.apply {

            title.text = "Upload profile"
            Glide.with(this@SharingFragment)
                .load(url)
                .into(image)
            backArrow.setOnClickListener {  onBackPressed.onBackPress()}
            upload.isVisible = true

            upload.setOnClickListener {
                alertDialog.show()
                val metadata = StorageMetadata.Builder().setCustomMetadata("type", "profile").build()
                val storagePathRef = FirebaseStorage.getInstance().reference
                    .child("users/$currentUserId/profile.jpg")
                val uploadTask = storagePathRef.putFile(Uri.parse(url), metadata)

                uploadTask.addOnProgressListener {

                    val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()

                    Toast.makeText(mActivity, "$progress% Uploading... ", Toast.LENGTH_SHORT).show()

                }.addOnSuccessListener {
                    mActivity.apply {
                        Toast.makeText(this, "Uploaded successful.", Toast.LENGTH_SHORT).show()
                        alertDialog.dismiss()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }

                }.addOnFailureListener{
                    Log.d("com.likon.gl.TAG", "onProgress: eeeeee $it")
                }

            }

        }
    }

    private fun setPostInfo(){
        binding.apply {

            title.text = "Share"
            Glide.with(this@SharingFragment)
                .load(url)
                .into(image)
            backArrow.setOnClickListener {  onBackPressed.onBackPress()}
            descriptionLayout.isVisible = true
            share.isVisible = true
            share.setOnClickListener {
                alertDialog.show()

                share.apply {
                    isEnabled = false
                    onEditorAction(EditorInfo.IME_ACTION_DONE)
                }

                CoroutineScope(Dispatchers.IO).launch {

                    val time = Date().time
                    val postId = ref.push().key ?: time.toString()
                    roomDBViewModel.insertUpload(UploadEntityModel(postId, url!!, description.text.toString(),0, time, false))
                    doubleCheck(postId)
                }

            }

        }

    }


    private  fun doubleCheck(postId : String){

        lifecycleScope.launchWhenResumed {

            roomDBViewModel.getUploadContentFlow(postId).collectLatest {

                if(it != null && it.post_id == postId){
                    mActivity.apply {
                        alertDialog.dismiss()
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
