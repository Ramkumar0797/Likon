package com.likon.gl.profile

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.common.*
import com.likon.gl.common.follow.FollowInfoFragment
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.interfaces.OnReselectedListener

class ProfileContainerFragment : Fragment(R.layout.fragment_profile_container),
    OnFragmentChangeListener, OnFragmentBackPressed, OnReselectedListener {

    private lateinit var onBackPressedListener: OnBackPressedListener

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is MainActivity){
            onBackPressedListener = context
        }
    }

    override fun onResume() {
        super.onResume()
        if(childFragmentManager.fragments.size == 0){
            setFirstFragment(ProfileFragment(this))
        }
    }

    private fun setFirstFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().apply {
            add(R.id.profile_fragment_container,fragment,"1")
            setReorderingAllowed(true)
            commit()
        }
    }

    override fun onChange(currentFragment: Fragment, addFragment: Int, bundle: Bundle?) {
        when(addFragment){


            R.integer.people_profile ->  onTransaction(currentFragment, PeopleProfileFragment(this,
                this).apply { arguments = bundle })
            R.integer.follow         ->  onTransaction(currentFragment, FollowInfoFragment(this,this).apply { arguments = bundle })
            R.integer.message        ->  onTransaction(currentFragment, MessageFragment(this).apply { arguments = bundle})
            R.integer.iconic_status  ->  onTransaction(currentFragment, IconicTimeLineFragment().apply { arguments = bundle})
            R.integer.profile_menu   ->  onTransaction(currentFragment, ProfileMenuFragment(this, this).apply { arguments = bundle })
            R.integer.special_post                ->  onTransaction(currentFragment, SpecialPostDaysFragment(this))
            R.integer.edit_profile                -> onTransaction(currentFragment, EditProfileFragment(this))
            R.integer.post                -> onTransaction(currentFragment, PostFragment(this,
                this).apply { arguments = bundle })
            R.integer.comments -> onTransaction(currentFragment, CommentsFragment(this,
                this).apply { arguments = bundle })
            R.integer.votes          -> onTransaction(currentFragment, VotesFragment(this, this).apply { arguments = bundle })
            R.integer.result                ->  onTransaction(currentFragment, ResultFragment().apply { arguments = bundle})
        }
    }

    private fun onTransaction(currentFragment: Fragment, addFragment: Fragment){
        childFragmentManager.apply {
            val index = fragments.size + 1
            beginTransaction().apply {
                hide(currentFragment)
                add(R.id.profile_fragment_container,addFragment,"$index")
                setReorderingAllowed(true)
                commit()
            }
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

    override fun onReselected() {
        childFragmentManager.apply {
            val index = fragments.size
            if(index > 1){
                beginTransaction().apply {
                    show(findFragmentByTag("1")!!)
                    for(i : Int in 2..index){
                        remove(findFragmentByTag("$i")!!)
                    }
                    setReorderingAllowed(true)
                    commit()
                }
            }
        }
    }

}