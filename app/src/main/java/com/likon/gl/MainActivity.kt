package com.likon.gl

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.UploadTask
import com.likon.gl.chat.ChatContainerFragment
import com.likon.gl.databinding.ActivityMainBinding
import com.likon.gl.home.HomeContainerFragment
import com.likon.gl.interfaces.*
import com.likon.gl.models.MessageEntity
import com.likon.gl.models.MessageModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.models.UsersEntity
import com.likon.gl.profile.ProfileContainerFragment
import com.likon.gl.search.SearchContainerFragment
import com.likon.gl.shots.ShotsContainerFragment
import com.likon.gl.viewModel.RoomDBViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest
import java.util.*


private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity(),
    OnBackPressedListener, OnShowAndDismissDialog,
    OnBottomNavVisibilityListener {

    var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            setUpload()
        }
    }


    private lateinit var mViewPager: ViewPager2
    private lateinit var binding: ActivityMainBinding
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((application as MyApplication).repository) }
    private lateinit var alertDialog : AlertDialog
    private var uploadTask : UploadTask? = null
    private val mAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var uploadJob: Job? = null
    lateinit var currentUserId : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val currentUser = mAuth.currentUser

        if(currentUser != null){
            currentUserId = currentUser.uid
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.apply {

            bottomView.apply {

                setOnItemSelectedListener {

                    val action = {index : Int -> mViewPager.setCurrentItem(index, false)}
                    when(it.itemId) {

                        R.id.home_container -> action(0)
                        R.id.search_container ->  action(1)
                        R.id.share_container -> { action(2)
//                            setVisibility(false)
                        }
                        R.id.chat_container ->  action(3)
                        R.id.profile_container -> action(4)

                    }
                    return@setOnItemSelectedListener true
                }

                setOnItemReselectedListener {

                    val action = {index : Int -> val fragment = supportFragmentManager.fragments[index]
                        if (fragment is OnReselectedListener)
                            fragment.onReselected()}
                    when(it.itemId) {

                        R.id.home_container -> {action(0)}
                        R.id.search_container -> { action(1)}
                        R.id.chat_container -> action(3)
                        R.id.profile_container -> action(4)

                    }
                }
            }

            mViewPager = pager.apply {
                    offscreenPageLimit = 5
                    isUserInputEnabled = false
                    adapter = MainPagerAdapter()
            }

            alertDialog = AlertDialog.Builder(this@MainActivity).apply {
                setView(R.layout.dialog_progress_layout)
                setCancelable(false)
            }.create()

        }

        getNewMessages(currentUserId)
        getUserInfo()
        setUpload()

    }

    private fun getNewMessages(uid : String) {
        db.collection("users").document(uid).collection("message")
            .whereEqualTo("read", false)
            .addSnapshotListener(MetadataChanges.INCLUDE) { value, error ->

                if (value != null) {

                    for(document in value){

                        val mge = document.toObject(MessageModel::class.java)
                        val setSever = { mge.message_id?.let {
                                db.collection("users").document(uid).collection("message")
                                    .document(it).update("read", true)
                            }
                        }

                        val setUserAndMge  = {  user : UsersEntity?, mess : MessageEntity? , insert : Boolean->
                            CoroutineScope(Dispatchers.IO).launch {
                                    roomDBViewModel.insertMgeCurrentUser(user, mess, insert)
                            }
                        }

                        val setOrUpdateUser  = {user : UsersEntity?, mess : MessageEntity? ->
                            CoroutineScope(Dispatchers.IO).launch {
                                roomDBViewModel.insertMgeSender(user, mess)
                            }
                        }

                      val tMge =  mge.message_id?.let{
                            MessageEntity(0, it, mge.sender, mge.con_with,
                                mge.message, mge.seen, mge.received, mge.date?.time)
                        }

                          val tUser = { id : String, count : Int, sender : String? ->

                            val state =  when {
                                  mge.seen -> "seen"
                                  mge.received -> "received"
                                  mge.date != null -> "date"
                                  else -> ""
                              }

                              UsersEntity(id, null, "", null, count,
                                  mge.message, sender, state) }


                          if (uid == mge.sender) {
                              if(mge.read || mge.seen){
                                  //update
                                  setUserAndMge(mge.con_with?.let {tUser(it, 0, uid)}, tMge, false)
                              }else{
                                  //insert
                                  setUserAndMge(mge.con_with?.let {tUser(it, 0, uid)}, tMge, true)
                              }

                              if (mge.seen) {
                                  setSever()
                              }
                          } else {
                              if (mge.date != null) {
                                  setSever()
                                  setOrUpdateUser(mge.sender?.let {tUser(it, 1, mge.sender)}, tMge)

                                  if(!mge.seen ){
                                      if(mge.sender != null && mge.message_id != null){
                                          setReadAndSeen(mge.sender, mge.message_id)
                                      }
                                  }
                              }
                          }


                    }
                }else{
                    Log.d(TAG, "getNewMessages: 77777777777777777777777777777777")
                }
            }
    }

    private fun setReadAndSeen(userId : String, mgeId : String){
        db.collection("users").document(userId).collection("message")
            .document(mgeId)
            .update("received" , true)
    }

    private fun getUserInfo(){

        lifecycleScope.launchWhenResumed {
            roomDBViewModel.getUsersFlow().collectLatest { users ->
                for(user in users){
                    user?.let {
                        db.collection("users").document(it.user_id)
                            .get().addOnSuccessListener { it1 ->
                                val userInfo = it1.toObject(UserInfoModel :: class.java)
                                userInfo?.user_id?.let {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        roomDBViewModel.updateUser(UsersEntity(it,userInfo.profile_image,
                                            userInfo.username, userInfo.gender) )
                                    }

                                }
                            }
                    }
                }
            }

        }
    }

    private inner class MainPagerAdapter : FragmentStateAdapter(this){
        override fun getItemCount(): Int = 5

        override fun createFragment(position: Int): Fragment {
            return when(position) {

                0 -> HomeContainerFragment()
                1 -> SearchContainerFragment()
                2 -> ShotsContainerFragment()
                3 -> ChatContainerFragment()
                4 -> ProfileContainerFragment()

                else -> HomeContainerFragment()
            }
        }
    }

    override fun onBackPressed() {

        val action = {index : Int -> val fragment = supportFragmentManager.fragments[index]
            if (fragment is OnFragmentBackPressed)
            fragment.onBackPress()}

        when(mViewPager.currentItem){

            0 -> action(0)
            1 -> action(1)
            2 -> action(2)
            3 -> action(3)
            4 -> action(4)
        }
    }

    override fun onBackPress(isMain: Boolean?) {
        if (isMain == true){
            super.onBackPressed()
            return
        }
        mViewPager.setCurrentItem(0, false)
        binding.bottomView.apply {
            selectedItemId = R.id.home_container
            visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("TAG", "onBackPress:  why size show extra one  ${supportFragmentManager.fragments.size}")
    }

    private fun setUpload(){

        if(uploadTask == null) {
            uploadJob?.cancel()
              uploadJob = CoroutineScope(Dispatchers.IO).launch {
                       val uploadContent = roomDBViewModel.getUpload()

                        if (uploadContent != null) {

                            val metadata = StorageMetadata.Builder().
                            setCustomMetadata("description", uploadContent.description)
                                .setCustomMetadata("type", "post").build()
                            val storagePathRef = FirebaseStorage.getInstance().reference.child("users/" + currentUserId + "/posts/" + uploadContent.post_id + "/image.jpg")
                          val  uploadTask = storagePathRef.putFile(Uri.parse(uploadContent.uri), metadata)

                            uploadTask.addOnProgressListener {

                                var id: String? = null
                                try {
                                    val strings: Array<String> = it.uploadSessionUri?.getQueryParameter("name")!!.split("/".toRegex()).toTypedArray()
                                    id = strings[3]
                                } catch (e: NullPointerException) {
                                    Log.d(TAG, "onProgress: eeeeeeeeeeeeeeeeeeeee")
                                }

                                val progress = (100 * it.bytesTransferred / it.totalByteCount).toInt()


                                Toast.makeText(this@MainActivity, "onProgress$progress", Toast.LENGTH_LONG).show()

                                if (progress in 1..99) {

                                    id?.let { it1 -> roomDBViewModel.updateUpload(progress, false, it1) }


                                } else if (progress == 100) {

                                    id?.let { it1 -> roomDBViewModel.updateUpload(progress, true, it1) }
                                }
                            }.addOnSuccessListener {
                                val id: String?
                                val strings: Array<String> = it.uploadSessionUri?.getQueryParameter("name")!!.split("/".toRegex()).toTypedArray()
                                id = strings[3]

//                                uploadTask = null
                                setUpload()
                            }

                        }
              }

        }
    }

    override fun setDialogState(state: Boolean) {
        if(state){
            alertDialog.show()
        }else{
            alertDialog.dismiss()
        }
    }

    override fun setVisibility(visibility: Boolean) {

        binding.bottomView.isVisible = visibility
    }
}