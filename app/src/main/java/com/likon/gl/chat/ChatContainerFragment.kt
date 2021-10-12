package com.likon.gl.chat

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.R
import com.likon.gl.common.MessageFragment
import com.likon.gl.home.MainFeedFragment
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener


private const val TAG = "ChatContainerFragment"

class ChatContainerFragment : Fragment(R.layout.fragment_chat_container), OnFragmentChangeListener,
    OnFragmentBackPressed{

    private lateinit var onBackPressedListener: OnBackPressedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnBackPressedListener){
            context.also { onBackPressedListener = it }
        }
    }

    override fun onResume() {
        super.onResume()

        if(childFragmentManager.fragments.size <= 0){
            addingFragment(ChatFragment(this))
        }

    }
    private   fun addingFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.apply {
            add(R.id.chat_fragment_container,fragment,"1")
            setReorderingAllowed(true)
            commit()
        }
    }

    override fun onBackPress() {
        childFragmentManager.apply {
            val size = fragments.size
            if(size <= 1  ){
                onBackPressedListener.onBackPress(null)
            }else{
                beginTransaction().apply {
                    remove(findFragmentByTag("$size")!!)
                    show(findFragmentByTag("${size-1}")!!)
                    setReorderingAllowed(true)
                    commit()
                }
            }
        }
    }

    private fun onTransaction(currentFragment: Fragment, addFragment: Fragment){
        childFragmentManager.apply {
            val index = fragments.size + 1
            beginTransaction().apply {
                hide(currentFragment)
                add(R.id.chat_fragment_container,addFragment,"$index")
                setReorderingAllowed(true)
                commit()
            }
        }
    }

    override fun onChange(currentFragment: Fragment, addFragment: Int, bundle: Bundle?) {
        when(addFragment){
            R.integer.message ->  onTransaction(currentFragment, MessageFragment(this).apply { arguments = bundle})
        }
    }
}