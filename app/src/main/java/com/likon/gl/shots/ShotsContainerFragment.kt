package com.likon.gl.shots


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.interfaces.*


private const val TAG = "ShareContainerFragment"
class ShotsContainerFragment : Fragment(R.layout.fragment_shots_container),
    OnFragmentChangeListener, OnFragmentBackPressed{


    private lateinit var onBackPressedListener: OnBackPressedListener

    private lateinit var onShowAndDismissDialog: OnShowAndDismissDialog

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is OnBackPressedListener){
            if ( context is MainActivity) {
                context.also {

                    onBackPressedListener = it
                    onShowAndDismissDialog = it
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()

        if(childFragmentManager.fragments.size <= 0){
//                    addingFragment()
        }else{
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


    private   fun addingFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.apply {
            add(R.id.shots_fragment_container,fragment,"1")
            setReorderingAllowed(true)
            commit()
        }
    }

    private fun onTransaction(currentFragment: Fragment, addFragment: Fragment){
        childFragmentManager.apply {
            val index = fragments.size + 1
            beginTransaction().apply {
                hide(currentFragment)
                add(R.id.shots_fragment_container,addFragment,"$index")
                setReorderingAllowed(true)
                commit()
            }
        }
    }

    override fun onChange(currentFragment: Fragment, addFragment: Int, bundle: Bundle?) {
        when(addFragment){


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


}