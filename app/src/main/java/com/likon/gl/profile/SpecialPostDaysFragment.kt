package com.likon.gl.profile

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.likon.gl.R
import com.likon.gl.databinding.FragmentSpecialPostDaysBinding
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.models.SpecialPosts
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.log

private const val TAG = "SpecialPostDaysFragment"

class SpecialPostDaysFragment(private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_special_post_days) {

    private var _binding: FragmentSpecialPostDaysBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private lateinit var functions : FirebaseFunctions
    private lateinit var currentCal : Calendar
    private  var currentYear  = 0
    private  var currentMonth  = 0
    private  var currentDay  = 0
    private lateinit var birthCal : Calendar
    private lateinit var iconicCal : Calendar
    private val dateFormat = SimpleDateFormat("dd MMM yyyy, EEE", Locale.getDefault())
    private val setDateToCal = {date : Date ->  Calendar.getInstance().apply { time = date }}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSpecialPostDaysBinding.bind(view)

        binding.apply {

            getTime()
            backArrow.setOnClickListener {
                onBackPressed.onBackPress()
            }
            retry.setOnClickListener {
                getTime()
                retry.isVisible = false
                progress.isVisible = true
            }
        }



    }

    private fun getTime(){

        //get time from server+++++++++++++++++++++++++++
        functions = Firebase.functions
        functions.getHttpsCallable("getTime")
            .call()
            .addOnCompleteListener{
                if(!it.isSuccessful){
                    //netWork Error

//                    showErrorDialog("Network error", "Please check your internet connection")
                    binding.apply {
                        progress.isVisible = false
                        retry.isVisible = true
                    }
                    Log.d(TAG, "setData: iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii77")

                }else{
                    Log.d(TAG, "setData: iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii89")

                    //current year date setting++++++++++++++++++++++
                    currentCal = setDateToCal(Date(it.result.data as Long))
                    currentYear = currentCal.get(Calendar.YEAR)
                    currentMonth = currentCal.get(Calendar.MONTH)
                    currentDay = currentCal.get(Calendar.DAY_OF_MONTH)
                    //current year date setting-----------------

                    getPostsInfo()
                }
            }
        //get time from server------------------
    }

    //getting special post info++++++++++++++++++
    private fun getPostsInfo(){     db.collection("users").document(auth.uid!!).collection("special_posts")
        .document("posts_info").get(Source.SERVER)
        .addOnSuccessListener { doc ->

            val postsInfo = doc.toObject(SpecialPosts::class.java)
            postsInfo?.let {
                setData(it)
            }

        }.addOnFailureListener {
            binding.apply {
                progress.isVisible = false
                retry.isVisible = true
            }
//            showErrorDialog("Network error", "Please check your internet connection")
        }

    }
    //getting special post info---------------

    private fun setData(postsInfo : SpecialPosts){
        binding.apply {


            postsInfo.date_of_birth?.let {

                birthCal = setDateToCal(it)
                birthDate.text = dateFormat.format(it)
                val birthMon = birthCal.get(Calendar.MONTH)
                val birthDay = birthCal.get(Calendar.DAY_OF_MONTH)

                startChecking(birthMon, birthDay, postsInfo.dob_last, birthNext, birthRemaining, birthPostInfo,
                    layBirthPost, layBirthRemain)
            }


            postsInfo.spd?.let {
                Log.d(TAG, "setData: iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii22")
                when(postsInfo.iconic_status){
                    "married" ->{
                        Log.d(TAG, "setData: iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii")
                        iconicDate.text = dateFormat.format(it)
                        iconicDay.text = "is your wedding day"

                    }
                    "valentine" ->{
                        iconicDate.text = "February 14"
                        iconicDay.text = "is Valentine's day"

                    }
                    "single" ->{
                        iconicDate.text = "February 29"
                        iconicDay.text = "is Bachelor's day"
                    }
                }
                iconicCal = setDateToCal(it)
                val iconicMon = iconicCal.get(Calendar.MONTH)
                val iconicDay = iconicCal.get(Calendar.DAY_OF_MONTH)
                startChecking(iconicMon, iconicDay, postsInfo.spd_last , iconicNext, iconicRemaining, iconicPostInfo,
                    layIconicPost, layIconicRemain)
            }

            progress.visibility = View.GONE
            layMain.visibility = View.VISIBLE
            layBottom.visibility = View.VISIBLE
        }
    }


    private fun startChecking(postMon : Int, postDay : Int, lastPostDate : Date?, nextDate : TextView,
                              remaining : TextView, postInfo : TextView, postLay : LinearLayout,  remainLay : LinearLayout){

        var temp = 0
        var lastPostDay = 0
        var lastPostMonth = 0

        if(lastPostDate != null) {
            val lastPostCal = setDateToCal(lastPostDate)
             temp = lastPostCal.get(Calendar.YEAR) - currentYear
            lastPostMonth = lastPostCal.get(Calendar.MONTH)
            lastPostDay = lastPostCal.get(Calendar.DAY_OF_MONTH)
        }

        if (temp < 0 || lastPostDate == null){
            //quota did not finished
            checkPostDate( postMon, postDay, nextDate, remaining, postInfo, postLay, remainLay)
        }else{
            //quota finished then next year
            val info : String? = if(lastPostMonth >= postMon && lastPostDay >= postDay){
                                    null
                                }else{
                                    "You already shared post successful, Please don't change date Repeatedly!"
                                }

            leapYearCheck(1, postMon, postDay, nextDate, remaining,
                postInfo, info)

        }
    }

    private fun checkPostDate(postMon : Int, postDay : Int, nextDate : TextView,
                              remaining : TextView, postInfo : TextView, postLay : LinearLayout,  remainLay : LinearLayout){
        if(postMon == currentMonth && postDay == currentDay){
            //time to share post
            remainLay.visibility = View.GONE
            postLay.visibility = View.VISIBLE

        }else if(postMon >= currentMonth && postDay > currentDay){
            //remaining

            leapYearCheck(0, postMon, postDay, nextDate, remaining, postInfo,null)

        }else{

            leapYearCheck(1, postMon, postDay, nextDate, remaining, postInfo,"Sorry you missed this year quota!")

        }
    }

    private fun leapYearCheck(yearToAdd : Int, postMon : Int, postDay : Int, nextDate : TextView, remaining : TextView,
                              postInfo : TextView, info : String?){

        if(postMon == 1 && postDay == 29){
            if(currentYear % 4 == 0){
                setNextDate(currentYear, postMon, postDay, nextDate, remaining)
                if(info != null){
                    postInfo.apply {
                        text = info
                        visibility = View.VISIBLE
                    }
                }
            }else{

                for(i in 1..5){

                    val temp2 = currentYear + i
                    if(temp2 % 4 == 0) {
                        setNextDate(temp2, postMon, postDay, nextDate, remaining)
                        break
                    }
                }
            }
        }else{

            setNextDate(currentYear + yearToAdd, postMon, postDay, nextDate, remaining)
            if(info != null){
                postInfo.apply {
                    text = info
                    visibility = View.VISIBLE
                }
            }

        }
    }

    private fun setNextDate(Year : Int, postMon : Int, postDay : Int, nextDate : TextView, remaining : TextView) {
        val nexPostDate = Calendar.getInstance().apply { set(Year, postMon, postDay, 0, 0, 0) }.time
        nextDate.text = dateFormat.format(nexPostDate)
        getTimeDifferent(nexPostDate, remaining)
    }

    private fun getTimeDifferent(date : Date, remaining : TextView){

        val secondsInMilli : Long = 1000
        val minutesInMilli: Long = secondsInMilli * 60
        val hoursInMilli: Long = minutesInMilli * 60
        val daysInMilli: Long = hoursInMilli * 24

        val left = {tag : String, differ : Long, tagValue : Long ->
            val value = differ / tagValue
            if(value > 0){
                remaining.text = value.toString() + tag
                true
            }else

                false
        }

        var different = date.time - currentCal.timeInMillis
        if(left("days", different, daysInMilli)){
            return
        }

        different %= daysInMilli
        if(left("hrs", different, hoursInMilli)){
            return
        }

        different %= hoursInMilli
        if(left("mints", different, minutesInMilli)){
            return
        }

        different %= minutesInMilli
        left("secs", different, secondsInMilli)

    }


    private fun showErrorDialog(title : String, message : String){

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }.show()
    }

}