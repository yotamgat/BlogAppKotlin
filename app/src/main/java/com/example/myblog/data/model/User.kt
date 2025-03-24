package com.example.myblog.data.model


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_table")
data class User(
    @PrimaryKey val id: String = "",
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = "android.resource://com.example.myblog/drawable/ic_profile_placeholder"
)