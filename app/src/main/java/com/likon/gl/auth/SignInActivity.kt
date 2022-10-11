package com.likon.gl.auth

import android.accounts.NetworkErrorException
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseNetworkException

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.GoogleAuthProvider

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.likon.gl.MainActivity
import com.likon.gl.R
import com.likon.gl.databinding.ActivityLogInBinding
import com.likon.gl.models.UserInfoModel


private const val TAG = "LogInActivity"
class SignInActivity : AppCompatActivity() {

    private lateinit var alertDialog : AlertDialog
    private lateinit var binding: ActivityLogInBinding
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->


            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)


            try {
                task.getResult(ApiException::class.java)?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                if (e.statusCode == CommonStatusCodes.NETWORK_ERROR) {
                    showNetworkError()
                }else{
                    Log.d(TAG, "eeeeeeeeeeeeeeee: $e ")
                   alertDialog.dismiss()
                    binding.signInButton.isEnabled = true
                }

            }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLogInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
//
//       val signInRequest = BeginSignInRequest.builder()
//            .setGoogleIdTokenRequestOptions(
//                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
//                    .setSupported(true)
//                    // Your server's client ID, not your Android client ID.
//                    .setServerClientId(getString(R.string.default_web_client_id))
//                    // Only show accounts previously used to sign in.
//                    .setFilterByAuthorizedAccounts(true)
//                    .build())
//            .build()

        GoogleSignIn.getClient(this, gso).signOut()
        val signInIntent = GoogleSignIn.getClient(this, gso).signInIntent

        binding.signInButton.setOnClickListener {
            it.isEnabled = false
            alertDialog = AlertDialog.Builder(this).apply {
                setView(R.layout.dialog_progress_layout)
                setCancelable(false)
            }.create()
            alertDialog.show()
            resultLauncher.launch(signInIntent)
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {

                        val userID = FirebaseAuth.getInstance().uid
                        if (userID != null) {
                            db.collection("users").document(userID)
                                    .get(Source.SERVER)
                                    .addOnSuccessListener { documentSnapshot ->

                                            val userInfoModel = documentSnapshot.toObject(
                                                UserInfoModel::class.java)

                                            if (userInfoModel?.user_id != null ) {
                                                val sharedPref = this.getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
                                                with (sharedPref.edit()) {
                                                    putString(getString(R.string.username_key), userInfoModel.username)
                                                    apply()
                                                }
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            } else {
                                                startActivity(Intent(this,NewUserActivity::class.java))
                                                finish()
                                            }

                                    }.addOnFailureListener {
                                        if(it is FirebaseNetworkException || it is NetworkErrorException){
                                            showNetworkError()
                                        }else{
                                            showUnknownError()
                                        }
                                    }
                        }
                    } else {
                        showUnknownError()
                    }
                }.addOnFailureListener {

                    if(it is FirebaseNetworkException || it is NetworkErrorException){
                        showNetworkError()
                    }else{
                        showUnknownError()
                    }

                }
    }

    private fun showNetworkError(){
        alertDialog.dismiss()
        MaterialAlertDialogBuilder(this)
            .setTitle("Network error!")
            .setMessage("Internet connection lost!")
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }.show()
        binding.signInButton.isEnabled = true
    }

    private fun showUnknownError(){
        alertDialog.dismiss()
        MaterialAlertDialogBuilder(this)
            .setTitle("Missing error!")
            .setMessage("Something wrong please try again!")
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }.show()
        binding.signInButton.isEnabled = true
    }


}


