package com.likon.gl.common.follow

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.databinding.FragmentFollowInfoBinding
import com.likon.gl.interfaces.OnBackPressedListener
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener
import com.likon.gl.models.UserInfoModel


class FollowInfoFragment(private val onFragmentChangeListener: OnFragmentChangeListener, private val onBackPressed : OnFragmentBackPressed) : Fragment() {

    private var _binding: FragmentFollowInfoBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val binding get() = _binding!!
    private var userInfoModel : UserInfoModel? = null
    private var bundle : Bundle? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            userInfoModel = it.getParcelable("user info")
            bundle = Bundle().apply { putParcelable("user info", userInfoModel ) }
        }




    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View {
        // Inflate the layout for this fragment
        _binding = FragmentFollowInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val followAdapter = FollowInfoPagerAdapter(this,bundle)
        var viewPager : ViewPager2
        binding.apply {
            viewPager = container.apply { adapter= followAdapter }
            username.text = userInfoModel?.username
            backArrow.setOnClickListener {
                onBackPressed.onBackPress()
            }

        }

        TabLayoutMediator(binding.tabs,viewPager){tab,position ->
            when(position){
                0 -> tab.text = "Following"
                1 -> tab.text = "Followers"
            }
        }.attach()

    }

    private inner class FollowInfoPagerAdapter(fragment: Fragment, bundle: Bundle?) : FragmentStateAdapter(fragment){

        val followersFragment = FollowersFragment(this@FollowInfoFragment, onFragmentChangeListener).apply {arguments = bundle}
        val followingFragment = FollowingFragment(this@FollowInfoFragment, onFragmentChangeListener).apply {arguments = bundle}

        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment {

            return  when(position){
                0 -> followingFragment
                1 -> followersFragment
                else -> followingFragment
            }

        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}