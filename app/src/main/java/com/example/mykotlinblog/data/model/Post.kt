package com.example.mykotlinblog.data.model

data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImageUrl: String = "",
    val postImageUrl: String = "",
    val description: String = ""
)