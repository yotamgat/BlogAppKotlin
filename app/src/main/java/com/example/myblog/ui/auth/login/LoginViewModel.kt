package com.example.myblog.ui.auth.login

import android.app.Application

import androidx.lifecycle.AndroidViewModel

import androidx.lifecycle.viewModelScope
import com.example.myblog.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) :  AndroidViewModel(application) {

    private val repository = AuthRepository(application.applicationContext)

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            repository.loginUser(email, password, onResult)
        }
    }
}