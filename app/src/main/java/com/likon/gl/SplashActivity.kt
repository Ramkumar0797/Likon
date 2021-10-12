package com.likon.gl

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.auth.NewUserActivity
import com.likon.gl.auth.SignInActivity
import com.likon.gl.models.UserInfoModel


class SplashActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val userId = auth.uid
        if(userId != null){
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { documentSnapshot ->

                    val userInfoModel = documentSnapshot.toObject(UserInfoModel::class.java)
                    if (userInfoModel?.user_id != null ) {
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()


                    } else {
                        startActivity(Intent(this, NewUserActivity::class.java))
                        finish()
                    }
                }
        }else{
            Handler(Looper.getMainLooper()).postDelayed({ startActivity(Intent(this,
                SignInActivity::class.java))
                finish()},3000)
        }

    }
}