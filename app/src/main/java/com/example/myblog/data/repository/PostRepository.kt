package com.example.myblog.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myblog.data.api.ChuckNorrisService

import com.example.myblog.data.api.CloudinaryService
import com.example.myblog.data.api.FirebaseService

import com.example.myblog.data.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class PostRepository(
    private val firebaseService: FirebaseService = FirebaseService(),
    private val cloudinaryService: CloudinaryService = CloudinaryService(),

) {

    suspend fun uploadPostToFirestore(imageUri: Uri, description: String, context: Context, onResult: (Boolean, String?) -> Unit) {
        withContext(Dispatchers.IO) {
            cloudinaryService.uploadImage(imageUri, context) { success, imageUrl ->
                if (success && imageUrl != null) {
                    val currentUserId = firebaseService.getCurrentUserId() ?: "Unknown User"

                    firebaseService.getUserById(currentUserId) { user ->
                        val post = Post(
                            id = firebaseService.generatePostId(),
                            userId = currentUserId,
                            userName = user?.name ?: "Current User",
                            userProfileImageUrl = user?.profileImageUrl ?: "android.resource://com.example.myblog/drawable/ic_profile_placeholder",
                            postImageUrl = imageUrl,
                            description = description
                        )

                        firebaseService.savePostToFirestore(post) { success, error ->
                            if (success) {
                                onResult(true, null)
                            } else {
                                onResult(false, error)
                            }
                        }
                    }
                } else {
                    onResult(false, "Failed to upload image to Cloudinary")
                }
            }
        }
    }

    suspend fun fetchPosts(onResult: (List<Post>) -> Unit) {
        firebaseService.getPosts { posts ->
            onResult(posts)
        }
    }

    suspend fun generatePostDescription(): String {
        return try {
            ChuckNorrisService.fetchRandomJoke() ?: "Failed to fetch joke"
        } catch (e: Exception) {
            e.printStackTrace()
            "Error fetching joke: ${e.message}"
        }
    }
    suspend fun toggleLike(postId: String, liked: Boolean, onResult: (Boolean, String?) -> Unit) {
        val currentUserId = firebaseService.getCurrentUserId()
        if (currentUserId != null) {
            firebaseService.getPostById(postId) { post ->
                if (post != null) {
                    val mutableLikes = post.likes.toMutableList()

                    if (liked) {
                        Log.d("PostRepository", "Adding like to post")
                        if (!mutableLikes.contains(currentUserId)) {
                            Log.d("PostRepository", "User ID: $currentUserId")
                            mutableLikes.add(currentUserId)
                        }
                    } else {
                        Log.d("PostRepository", "Removing like from post")
                        mutableLikes.remove(currentUserId)
                    }

                    firebaseService.updatePostLikes(postId, mutableLikes) { success, error ->
                        onResult(success, error)
                    }
                } else {
                    onResult(false, "Post not found")
                }
            }
        } else {
            onResult(false, "User not logged in")
        }
    }
    suspend fun updatePostWithImage(postId: String, imageUri: Uri, description: String, context: Context, onResult: (Boolean, String?) -> Unit) {
        cloudinaryService.uploadImage(imageUri, context) { success, imageUrl ->
            if (success && imageUrl != null) {
                firebaseService.updatePost(postId, imageUrl, description) { result, error ->
                    onResult(result, error)
                }
            } else {
                onResult(false, "Failed to upload image")
            }
        }
    }

    suspend fun updatePostDescription(postId: String, description: String, onResult: (Boolean, String?) -> Unit) {
        firebaseService.updatePostDescription(postId, description) { success, error ->
            onResult(success, error)
        }
    }

    suspend fun deletePost(postId: String, onResult: (Boolean, String?) -> Unit) {
        withContext(Dispatchers.IO) {
            firebaseService.getPostById(postId) { post ->
                if (post != null) {

                    cloudinaryService.deleteImage(post.postImageUrl) { imageDeleted, error ->
                        if (imageDeleted) {

                            firebaseService.deletePost(postId) { success, error ->
                                onResult(success, error)
                            }
                        } else {
                            onResult(false, "Failed to delete image from Cloudinary: $error")
                        }
                    }
                } else {
                    onResult(false, "Post not found")
                }
            }
        }
    }
    fun listenToAllPosts(onResult: (List<Post>) -> Unit) {
        firebaseService.listenToAllPosts(onResult)
    }
}
