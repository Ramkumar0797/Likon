package com.likon.gl.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.likon.gl.MainActivity
import com.likon.gl.MyApplication
import com.likon.gl.R
import com.likon.gl.RoomDBViewModelFactory
import com.likon.gl.databinding.FragmentPeopleProfileBinding
import com.likon.gl.databinding.FragmentPostBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel
import com.likon.gl.models.VoteModel
import com.likon.gl.models.VotesEntityModel
import com.likon.gl.viewModel.PostsViewModel
import com.likon.gl.viewModel.RoomDBViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class PostFragment(private val onFragmentChangeListener: OnFragmentChangeListener,
                   private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_post), View.OnClickListener {

    private var _binding: FragmentPostBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private lateinit var  userInfo : UserInfoModel
    private lateinit var post : PostModel
    private lateinit var mActivity: Activity
    private val mContext get() = mActivity
    private val roomDBViewModel : RoomDBViewModel by viewModels{  RoomDBViewModelFactory((mContext.application as MyApplication).repository) }
    private lateinit var currentUId : String



    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            currentUId = context.currentUserId
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TAG", "onCreate: ffffffffffffffffffffffffffffffffffff")

        arguments?.let {
            userInfo = it.getParcelable("user info")!!
            post = it.getParcelable("post")!!
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPostBinding.bind(view)
        binding.apply {
            iconicStatus.setImageResource(getIconicImage(userInfo.iconic_status, userInfo.gender))
            username.text = userInfo.username
            val getImage = {gender : String?, profile : String? ->
                profile ?: if(gender == "male") R.raw.male else R.raw.female
            }
            if(userInfo.profile_image == null){
                profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
            }

            Glide.with(this@PostFragment)
                .load(getImage(userInfo.gender, userInfo.profile_image))
                .into(profileImage)
            votes.setOnClickListener(this@PostFragment)
            up.setOnClickListener(this@PostFragment)
            down.setOnClickListener(this@PostFragment)
            result.setOnClickListener(this@PostFragment)
            comments.setOnClickListener(this@PostFragment)
            backArrow.setOnClickListener(this@PostFragment)

            lifecycleScope.launchWhenResumed {
                post.content_id?.let { id ->
                    roomDBViewModel.getVoteFlow(id).collectLatest { value ->
                        votes.text =
                            getString(R.string.votes, value?.votes_count)
                        val lVote = { upVote: Boolean, downVote: Boolean ->
                            up.isChecked = upVote
                            down.isChecked = downVote
                        }
                        when (value?.vote) {
                            true -> {
                                lVote(true, false)
                            }
                            false -> {
                                lVote(false, true)
                            }
                            else -> {
                                lVote(false, false)
                            }
                        }
                    }
                }
            }

        }
        getData()


    }

    private fun getData(){
        binding.apply {

                                Glide.with(mContext)
                                    .load(post.image_url)
                                    .into(imagePost)
                             post.type.let { type ->
                                    if (type == 1L) {
                                        Glide.with(mContext)
                                            .load(R.raw.leaf_fall_red)
                                            .into(background)
                                        specialText.isVisible = true
                                    }
                                }
                                comments.text = getString(R.string.comments, post.comments)
                                description.text = post.description
                                votes.text = post.votes.toString()


                                loader.isVisible = false
                                postLay.isVisible = true

            }

    }


    private fun getIconicImage(iconicStatus: String?, gender : String?): Int {

        val getImage = {temp : String? -> if(temp == "male") R.raw.msingle else R.raw.fsingle}
        return when(iconicStatus){


            "single" -> getImage(gender)

            "valentine" -> R.raw.heart

            "married" -> R.raw.couple

            else -> 0
        }

    }

    override fun onClick(p0: View?) {
        binding.apply {

            when(p0?.id){
                up.id        -> onUpVoteClick(up.isChecked, down.isChecked, post.content_id, userInfo.user_id)
                down.id      -> onDownVoteClick(up.isChecked, down.isChecked, post.content_id, userInfo.user_id)
                backArrow.id -> onBackPressed.onBackPress()
                votes.id     ->   onFragmentChangeListener.onChange(this@PostFragment,
                    R.integer.votes, Bundle().apply { putStringArray("votesInfo",
                        arrayOf(userInfo.user_id, post.content_id) )})
                comments.id ->  onFragmentChangeListener.onChange(this@PostFragment, R.integer.comments,
                    Bundle().apply { putStringArray("commentsInfo", arrayOf(userInfo.user_id, post.content_id) )})
                result.id ->  onFragmentChangeListener.onChange(this@PostFragment, R.integer.result, Bundle().apply { putStringArray("resultInfo",
                    arrayOf(userInfo.user_id, post.content_id, post.image_url) )})
            }


        }
    }

    private fun onUpVoteClick(upState: Boolean, downState: Boolean, postId: String?, userId: String?) {

        if(upState && !downState){
            setData("increment", true, postId, userId )

        }else if(upState && downState){
            setData("update", true, postId, userId )

        }else if(!upState && !downState){
            setData("decrement", null, postId, userId )
        }

    }

    private fun onDownVoteClick(upState: Boolean, downState: Boolean, postId: String?, userId: String?) {

        if(downState && !upState){
            setData("increment", false, postId, userId )

        }else if(downState && upState){
            setData("update", false, postId, userId )

        }else if(!downState && !upState){
            setData("decrement", null, postId, userId )

        }
    }

    private fun setData(job : String, vote : Boolean?, postId: String?, userId: String?){

        CoroutineScope(Dispatchers.IO).launch {

            val setVote = {pid : String, vote : Boolean? ->
                val docPath =   db.document("users/$userId/posts/$pid/votes/$currentUId")
                if(vote != null){
                    docPath.set(hashMapOf("vote" to vote, "time" to FieldValue.serverTimestamp()), SetOptions.merge())
                }else{
                    docPath.delete()
                }
            }
            when(job){

                "increment" ->{
                    postId?.let {pId -> roomDBViewModel.updateVote(pId, vote, 1)
                        setVote(pId, vote)
                    }
                }
                "decrement" ->{
                    postId?.let {pId -> roomDBViewModel.updateVote(pId, vote, -1)
                        setVote(pId, null)
                    }
                }
                "update" ->{
                    postId?.let {pId -> roomDBViewModel.updateVoteState(pId, vote!!)
                        setVote(pId, vote)
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