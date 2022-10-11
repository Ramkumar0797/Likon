package com.likon.gl

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.likon.gl.repository.RoomDBRepository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class MyApplication : Application() {

    // No need to cancel this scope as it'll be torn down with the process
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    private val database by lazy { User.getDatabase(this) }
    val repository by lazy { RoomDBRepository(database.userDao()) }
    val fireStoreDB by lazy { FirebaseFirestore.getInstance()  }
}