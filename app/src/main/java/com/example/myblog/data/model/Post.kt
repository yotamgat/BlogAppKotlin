// בתוך Post.kt
package com.example.myblog.data.model

import android.os.Parcelable

import kotlinx.android.parcel.Parcelize

@Parcelize
data class Post(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userProfileImageUrl: String = "",
    val postImageUrl: String = "",
    val description: String = "",
    val likes: List<String> = emptyList(),
    val comments: Int = 0
) : Parcelable

