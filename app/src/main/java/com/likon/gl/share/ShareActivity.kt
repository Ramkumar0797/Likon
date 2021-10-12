package com.likon.gl.share

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.likon.gl.R
import com.likon.gl.interfaces.OnFragmentBackPressed
import com.likon.gl.interfaces.OnFragmentChangeListener

class ShareActivity : AppCompatActivity(), OnFragmentChangeListener, OnFragmentBackPressed {


    private lateinit var type : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_share)

        type = intent.getStringExtra("type").toString()
        Log.d("vvvvvvvvvvvvvvvvv", "onCreate: $type")

    }

    override fun onChange(currentFragment: Fragment, addFragment: Int, bundle: Bundle?) {
        when(addFragment){

            R.integer.selected_folder -> onTransaction(currentFragment,
                ShareSelectedFolderFragment(this, this).apply { arguments = bundle })

            R.integer.share -> {

                val content = bundle?.getString("content")

                onTransaction(currentFragment,
                    SharingFragment(this).apply { arguments = Bundle().apply {
                        putString("content", content )
                        putString("type", type )
                    } })
            }

        }
    }

    override fun onBackPress() {
        this@ShareActivity.onBackPressed()

    }

    override fun onBackPressed() {

        supportFragmentManager.apply {
            val size = fragments.size
            if(size <= 2  ){
                super.onBackPressed()
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

    override fun onResume() {
        super.onResume()

        if(supportFragmentManager.fragments.size <= 0){

            Log.d("TAG", "onBackPress: why size show extra one ${supportFragmentManager.fragments.size}")
          requestPermission()
        }
    }

    private fun addFirstFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.apply {
            add(R.id.share_container,ShareGalleryFragment(this@ShareActivity,
                this@ShareActivity),"2")
            setReorderingAllowed(true)
            commit()
        }
    }

    private fun onTransaction(currentFragment: Fragment, addFragment: Fragment){
        supportFragmentManager.apply {
            val index = fragments.size + 1
            beginTransaction().apply {
                hide(currentFragment)
                add(R.id.share_container,addFragment,"$index")
                setReorderingAllowed(true)
                commit() }
        }
    }

    private fun requestPermission(){
        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
                addFirstFragment()

            }
            shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
//            showInContextUI(...);
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }



    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                addFirstFragment()
            } else {
                setResult(Activity.RESULT_CANCELED)
                finish()

                // Explain to the user that the feature is unavailable because the
                // features requires a permission that the user has denied. At the
                // same time, respect the user's decision. Don't link to system
                // settings in an effort to convince the user to change their
                // decision.
            }
        }
}