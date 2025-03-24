package com.example.myblog.data.repository


import android.content.Context
import android.net.Uri
import android.util.Log
import com.example.myblog.data.api.CloudinaryService
import com.example.myblog.data.api.FirebaseService
import com.example.myblog.data.local.AppDatabase
import com.example.myblog.data.model.Post
import com.example.myblog.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext



class ProfileRepository(
    private val firebaseService: FirebaseService = FirebaseService(),
    private val cloudinaryService: CloudinaryService = CloudinaryService(),
    context: Context
) {
    private val userDao = AppDatabase.getDatabase(context).userDao()

    fun getCurrentUser(onResult: (User?) -> Unit) {
        val currentUserId = firebaseService.getCurrentUserId()
        if (currentUserId != null) {
            firebaseService.getUserById(currentUserId) { user ->
                if (user != null) {
                    onResult(user)

                    saveUserLocally(user)
                } else {
                    getUserFromLocalDB(currentUserId, onResult)
                }
            }
        } else {
            onResult(null)
        }
    }

    private fun saveUserLocally(user: User) {
        Log.d("ProfileRepository", "Saving user to Room: $user")
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            userDao.insert(user)
        }
    }
    private fun getUserFromLocalDB(userId: String, onResult: (User?) -> Unit) {
        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
            val user = userDao.getUser(userId).firstOrNull() // משיגים את הערך הראשון מה-Flow
            kotlinx.coroutines.GlobalScope.launch(Dispatchers.Main) {
                onResult(user)
                Log.d("ProfileRepository", "Fetched user from Room: $user")
            }
        }
    }

    fun getUserPosts(userId: String, onResult: (List<Post>) -> Unit) {
        firebaseService.getPosts { posts ->
            val userPosts = posts.filter { it.userId == userId }
            onResult(userPosts)
        }
    }

    suspend fun updateUserProfile(newName: String, newImageUri: Uri?, context: Context, onResult: (Boolean, String?) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val currentUserId = firebaseService.getCurrentUserId()
                if (currentUserId != null) {
                    firebaseService.getUserById(currentUserId) { user ->
                        if (user != null) {
                            if (newImageUri != null) {
                                // העלאת תמונה חדשה ל-Cloudinary
                                cloudinaryService.uploadImage(newImageUri, context) { success, imageUrl ->
                                    if (success && imageUrl != null) {
                                        Log.d("ProfileRepository", "Image uploaded successfully: $imageUrl")

                                        // עדכון פרטי המשתמש עם ה-URL החדש
                                        saveUserLocally(user.copy(name = newName, profileImageUrl = imageUrl))
                                        val updatedUser = user.copy(name = newName, profileImageUrl = imageUrl)
                                        firebaseService.saveUserToFirestore(updatedUser) { saveSuccess, saveError ->
                                            if (saveSuccess) {
                                                Log.d("ProfileRepository", "User updated successfully in Firestore")

                                                // עדכון כל הפוסטים של המשתמש
                                                updateUserPosts(currentUserId, newName, imageUrl) { postUpdateSuccess, postUpdateError ->
                                                    if (postUpdateSuccess) {
                                                        Log.d("ProfileRepository", "User posts updated successfully")
                                                        onResult(true, null)
                                                    } else {
                                                        onResult(false, "Failed to update user posts: $postUpdateError")
                                                    }
                                                }
                                            } else {
                                                onResult(false, "Failed to save user to Firestore: $saveError")
                                            }
                                        }
                                    } else {
                                        onResult(false, "Failed to upload image to Cloudinary")
                                    }
                                }
                            } else {
                                // אם אין תמונה חדשה, רק מעדכנים את השם
                                val updatedUser = user.copy(name = newName)
                                firebaseService.saveUserToFirestore(updatedUser) { saveSuccess, saveError ->
                                    if (saveSuccess) {
                                        Log.d("ProfileRepository", "User updated successfully in Firestore")

                                        // עדכון הפוסטים הקיימים של המשתמש
                                        updateUserPosts(currentUserId, newName, user.profileImageUrl) { postUpdateSuccess, postUpdateError ->
                                            if (postUpdateSuccess) {
                                                Log.d("ProfileRepository", "User posts updated successfully")
                                                onResult(true, null)
                                            } else {
                                                onResult(false, "Failed to update user posts: $postUpdateError")
                                            }
                                        }
                                    } else {
                                        onResult(false, "Failed to save user to Firestore: $saveError")
                                    }
                                }
                            }
                        } else {
                            onResult(false, "User not found")
                        }
                    }
                } else {
                    onResult(false, "User not logged in")
                }
            } catch (e: Exception) {
                Log.e("ProfileRepository", "Error updating profile: ${e.message}")
                onResult(false, e.message)
            }
        }
    }


    fun updateUserPosts(userId: String, newName: String?, newProfileImageUrl: String?, onResult: (Boolean, String?) -> Unit) {
        firebaseService.getPosts { posts ->
            val userPosts = posts.filter { it.userId == userId }

            if (userPosts.isEmpty()) {
                onResult(false, "No posts found for user")
                return@getPosts
            }

            userPosts.forEach { post ->
                val updatedPost = post.copy(
                    userName = newName ?: post.userName,
                    userProfileImageUrl = newProfileImageUrl ?: post.userProfileImageUrl
                )

                firebaseService.savePostToFirestore(updatedPost) { success, error ->
                    if (!success) {
                        onResult(false, error)
                        return@savePostToFirestore
                    }
                }
            }

            // If we get here, all updates succeeded
            onResult(true, null)
        }
    }
    fun listenToUserPosts(userId: String, onPostsUpdated: (List<Post>) -> Unit) {
        firebaseService.listenToPosts(userId) { posts ->
            onPostsUpdated(posts)
        }
    }
}
