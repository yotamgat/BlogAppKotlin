package com.example.mykotlinblog.data.api

import android.util.Log
import com.example.mykotlinblog.data.model.Post
import com.example.mykotlinblog.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun registerUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }
    fun saveUserToFirestore(user: User, onResult: (Boolean, String?) -> Unit) {
        Log.d("FirebaseService", "Saving user to Firestore: ${user.id}")
        firestore.collection("users").document(user.id)
            .set(user)
            .addOnSuccessListener {
                Log.d("FirebaseService", "User saved successfully")
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseService", "Failed to save user: ${e.message}")
                onResult(false, e.message)
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun getPosts(onResult: (List<Post>) -> Unit) {
        firestore.collection("posts")
            .get()
            .addOnSuccessListener { snapshot ->
                val posts = snapshot.toObjects(Post::class.java)
                onResult(posts)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }
}