package com.likon.gl.common

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.*
import com.likon.gl.databinding.FragmentMessageBinding
import com.likon.gl.databinding.MessageAdapterBinding
import com.likon.gl.interfaces.OnBottomNavVisibilityListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.models.MessageEntity
import com.likon.gl.models.UserInfoModel
import com.likon.gl.repository.RoomDBRepository
import com.likon.gl.viewModels.MessageViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MessageFragment"

class MessageFragment( private val onBackPressed : OnFragmentBackPressed) : Fragment(R.layout.fragment_message), View.OnClickListener {

    private var _binding: FragmentMessageBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var userInfo : UserInfoModel? = null
    private lateinit var mActivity: Activity
    private lateinit var onBottomNavVisibilityListener: OnBottomNavVisibilityListener
    private val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")

    private lateinit var roomDB : RoomDBRepository
    private val viewModel : MessageViewModel
            by viewModels{  ViewModelFactory(null, roomDB, null) }


    private val messageList = MessageListAdapter()
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            mActivity = context
            onBottomNavVisibilityListener = context
            val myApplication =  (mActivity.application as MyApplication)
            roomDB = myApplication.repository
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userInfo = it.getParcelable("user info")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMessageBinding.bind(view)
        binding.apply {
            username.text = userInfo?.username
            backArrow.setOnClickListener(this@MessageFragment)
            send.setOnClickListener(this@MessageFragment)
            input.setOnClickListener(this@MessageFragment)
            val getImage = {gender : String?, profile : String? ->
                profile ?: if(gender == "male") R.raw.male else R.raw.female
            }
            if(userInfo?.profile_image == null){
                profileImage.scaleType = ImageView.ScaleType.FIT_CENTER
            }
            Glide.with(this@MessageFragment)
                .load(getImage(userInfo?.gender, userInfo?.profile_image))
                .into(profileImage)

            messages.apply {

                setHasFixedSize(true)
                adapter = messageList
            }


        }


        lifecycleScope.launchWhenResumed {

            userInfo?.user_id?.let { it ->
                viewModel.updateMgeCountNull(it)
                viewModel.getMessage(it).collectLatest { values ->
                    messageList.submitData(values)
                }
            }
        }
    }


    private fun setReadAndSeen(userId : String, mgeId : String){
        db.collection("users").document(userId).collection("message")
            .document(mgeId)
            .update("seen" , true)
    }

    override fun onResume() {
        super.onResume()
        mActivity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        onBottomNavVisibilityListener.setVisibility(false)

    }

    override fun onStop() {
        super.onStop()
        onBottomNavVisibilityListener.setVisibility(true)
    }

    override fun onClick(p0: View?) {

        when(p0?.id){

            binding.backArrow.id -> {
                hideKeyBord()
                onBackPressed.onBackPress()
            }

            binding.input.id -> {

            }
            binding.send.id -> {

                binding.input.apply {
                    val mge = this.text.toString()
                    if(mge != ""){
                        this.setText("")
                        dbWrite(mge)
                        hideKeyBord()
                        if(messageList.itemCount >= 2){
                            binding.messages.smoothScrollToPosition(messageList.itemCount -1)
                        }

                    }
                }

            }
        }
    }

    private fun dbWrite(message : String) {

        val messageID = ref.push().key ?: Date().time.toString()

        userInfo?.user_id?.let { rID ->

            auth.uid?.let { sID ->

                val sRef = db.collection("users").document(sID).collection("message").document(messageID)
                val rRef = db.collection("users").document(rID).collection("message").document(messageID)

                CoroutineScope(Dispatchers.IO).launch {
                    db.runBatch {

                        it.set(sRef, hashMapOf("message_id" to messageID,
                            "message" to message,
                            "date" to FieldValue.serverTimestamp(),
                            "sender" to sID,
                            "con_with" to rID,
                            "seen" to false,
                            "received" to false,
                            "read" to false,

                        )
                        )

                        it.set(rRef, hashMapOf("message_id" to messageID,
                            "message" to message,
                            "date" to FieldValue.serverTimestamp(),
                            "sender" to sID,
                            "con_with" to sID,
                            "read" to false  ) )

                    }

                }

            }

        }
    }

    private fun hideKeyBord(){
//        binding.messages.scrollToPosition(messageList.itemCount -1)
        binding.input.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                this.windowInsetsController?.hide(WindowInsets.Type.ime())
            } else {
                if(this.isFocused){
                    val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?
                    imm?.hideSoftInputFromWindow(this.windowToken ,0)
                }
            }
        }
    }


    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<MessageEntity>() {
            override fun areItemsTheSame(oldConcert: MessageEntity,
                                         newConcert: MessageEntity
            ) = oldConcert.message_id == newConcert.message_id

            override fun areContentsTheSame(oldConcert: MessageEntity,
                                            newConcert: MessageEntity
            ) = oldConcert == newConcert
        }
    }


    inner class MessageListAdapter :  PagingDataAdapter<MessageEntity, MessageListAdapter.MessageViewHolder>(DIFF_CALLBACK) {
        private val uID  = auth.uid
        inner class MessageViewHolder(private val messageAdapterBinding: MessageAdapterBinding) : RecyclerView.ViewHolder(messageAdapterBinding.root),
            View.OnClickListener {

            fun bind(messageEntity: MessageEntity?, previousEntity: MessageEntity?) {

                messageAdapterBinding.apply {

                    messageEntity?.let {

                        val time =  it.date?.let {time -> timeFormat.format(Date(time)) }
                        val date =  it.date?.let {date -> dateFormat.format(Date(date)) }
                        val previousDate = previousEntity?.date?.let { preDate -> dateFormat.format(Date(preDate)) }

                        val action = {receiveLay : Boolean, sendLay : Boolean, mge : TextView ->
                            receiveLayout.isVisible = receiveLay
                            sendLayout.isVisible = sendLay
                            mge.text = it.message

                            if(previousDate != date){
                                timeLabel.apply {
                                    text = date
                                    visibility = View.VISIBLE }
                            }else{
                                timeLabel.visibility = View.GONE
                            }

                        }


                        Log.d(TAG, "bind: ssssssssssaaa $date")
                        Log.d(TAG, "bind: eeeeeeeeeeeeee $time")



                        if(it.sender == uID ){
                            action(false, true, sendMessage)
                            sTime.text  = time
                            sendState.setImageResource(
                                when {
                                    it.seen -> R.drawable.ic_baseline_done_all_24
                                    it.received -> R.drawable.ic_baseline_done_all_light_24
                                    it.date != null -> R.drawable.ic_baseline_done_24
                                    else -> R.drawable.ic_baseline_more_horiz_24
                                }
                            )

                        }else{
                            action(true, false, receiveMessage)
                            rTime.text  = time
                            if(!it.seen){
                                it.sender?.let { it1 -> setReadAndSeen(it1, it.message_id) }
                            }
                        }
                    }

                }
            }
            override fun onClick(p0: View?) {
            }
        }

        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            var temp : MessageEntity? = null
            try {
                  temp = getItem(position-1)
            }catch (e :Exception){

            }

            holder.bind(getItem(position),temp)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val binding = MessageAdapterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return MessageViewHolder(binding)
        }

    }

}