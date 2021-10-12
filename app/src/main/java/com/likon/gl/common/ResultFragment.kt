package com.likon.gl.common

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.likon.gl.R
import com.likon.gl.databinding.FragmentMessageBinding
import com.likon.gl.databinding.FragmentResultBinding
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.models.PostModel
import com.likon.gl.models.UserInfoModel


private const val TAG = "ResultFragment"
class ResultFragment : Fragment(R.layout.fragment_result) {

    private var _binding: FragmentResultBinding? = null
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var userId : String? = null
    private var postId : String? = null
    private var imageUrl : String? = null




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getStringArray("resultInfo").apply {

            userId = this?.get(0)
            postId = this?.get(1)
            imageUrl = this?.get(2)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResultBinding.bind(view)

        getData()
    }

    private fun getData(){
        binding.apply {

            resultLoader.visibility = View.VISIBLE
            retry.visibility = View.GONE
            db.collection("users").document(userId!!)
                .get(Source.SERVER).addOnSuccessListener { userRef ->

                    db.collection("users").document(userId!!).collection("posts")
                        .document(postId!!).get(Source.SERVER).addOnSuccessListener {
                            val post = it.toObject(PostModel::class.java)
                            val user = userRef.toObject(UserInfoModel ::class.java)

                            lifecycleScope.launchWhenResumed {
                                post?.let { it1 -> setData(it1, user) }
                            }
                        }.addOnFailureListener{ lifecycleScope.launchWhenResumed {onFail()}}

                }.addOnFailureListener{lifecycleScope.launchWhenResumed {onFail()}}



            }
    }
    private fun onFail(){
        binding.apply {
            resultLoader.visibility = View.GONE
            retry.apply {
                visibility = View.VISIBLE
                setOnClickListener {
                    getData()
                }
            }
        }
    }

    private fun setData(post : PostModel, userInfo : UserInfoModel?) {
        binding.apply {
            Glide.with(this@ResultFragment)
                .load(imageUrl)
                .into(imagePost)
            Glide.with(this@ResultFragment)
                .load(userInfo?.profile_image)
                .into(profileImage)
            totalVotes.text = post.votes.toString()
            username.text = userInfo?.username

            val percent = { vote : Long ->  (vote.toFloat() / post.votes * 100).toInt()}
            val rate = { vote : Long ->  vote.toFloat() / post.votes * 5}

            upProgress.progress = percent(post.up)
            downProgress.progress = percent(post.down)
            rating.rating = rate(post.up)
            ratingLabel.text = String.format("%.1f", rate(post.up) )

            resultLoader.visibility = View.GONE
            resultLay.visibility  = View.VISIBLE

        }
    }


}