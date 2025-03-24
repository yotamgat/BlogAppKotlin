package com.example.myblog.data.repository

import android.content.Context
import android.util.Log
import com.example.myblog.data.api.FirebaseService
import com.example.myblog.data.local.AppDatabase
import com.example.myblog.data.local.daos.UserDao
import com.example.myblog.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthRepository(context: Context, private val firebaseService: FirebaseService = FirebaseService()) {

    private val userDao: UserDao = AppDatabase.getDatabase(context).userDao()
    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseService.loginUser(email, password, onResult)
    }

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        firebaseService.registerUser(email, password, onResult)
    }

    fun saveUserToFirestore(userId: String, name: String, email: String, onResult: (Boolean, String?) -> Unit) {
        val user = User(id = userId, name = name, email = email)
        firebaseService.saveUserToFirestore(user, onResult)
    }

    fun getCurrentUserId(): String? {
        Log.d("AuthRepository", "Getting current user ID");
        val currentUserId = firebaseService.getCurrentUserId()
        if (currentUserId != null) {
            saveCurrentUserLocally(currentUserId) // שמירת המשתמש ב- Room
        }
        return currentUserId
    }

    private fun saveCurrentUserLocally(userId: String) {
        firebaseService.getUserById(userId) { user ->
            if (user != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.insert(user) // שמירה ב-Room
                    Log.d("AuthRepository", "User saved to Room: $user")
                }
            } else {
                Log.e("AuthRepository", "Failed to retrieve user from Firebase")
            }
        }
    }

}