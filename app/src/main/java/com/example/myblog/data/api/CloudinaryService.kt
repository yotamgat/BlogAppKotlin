package com.example.myblog.data.api

import android.content.Context
import android.net.Uri
import android.util.Log
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.myblog.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CloudinaryService {

    private val cloudinary = Cloudinary(
        mapOf(
            "cloud_name" to BuildConfig.CLOUD_NAME,
            "api_key" to BuildConfig.API_KEY,
            "api_secret" to BuildConfig.API_SECRET
        )
    )

    fun uploadImage(imageUri: Uri, context: Context, onResult: (Boolean, String?) -> Unit) {
        try {
            Log.d("CloudinaryService", "Trying to open InputStream for URI: $imageUri")

            // פתיחת InputStream
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                Log.e("CloudinaryService", "InputStream is null for URI: $imageUri")
                onResult(false, "InputStream is null")
                return
            }

            // בדיקת זמינות נתונים
            val availableBytes = inputStream.available()
            if (availableBytes > 0) {
                Log.d("CloudinaryService", "InputStream is valid with $availableBytes bytes available")
            } else {
                Log.e("CloudinaryService", "InputStream is empty")
                onResult(false, "InputStream is empty")
                return
            }


            CoroutineScope(Dispatchers.IO).launch {
                try {
                    Log.d("CloudinaryService", "Starting upload to Cloudinary")
                    val uploadResult = cloudinary.uploader().upload(inputStream, ObjectUtils.emptyMap())
                    val imageUrl = uploadResult["secure_url"] as? String

                    withContext(Dispatchers.Main) {
                        if (imageUrl != null) {
                            Log.d("CloudinaryService", "Image uploaded successfully: $imageUrl")
                            onResult(true, imageUrl)
                        } else {
                            Log.e("CloudinaryService", "Upload failed: null URL returned")
                            onResult(false, "Upload failed: No URL")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CloudinaryService", "Error uploading image: ${e.message}")
                    withContext(Dispatchers.Main) {
                        onResult(false, e.message)
                    }
                } finally {
                    inputStream.close()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CloudinaryService", "Exception during upload: ${e.message}")
            onResult(false, e.message)
        }
    }
    fun deleteImage(imageUrl: String, onResult: (Boolean, String?) -> Unit) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {

                    val publicId = imageUrl.substringAfterLast("/").substringBeforeLast(".")
                    val result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())

                    val success = result["result"] == "ok" || result["result"] == "not_found"
                    withContext(Dispatchers.Main) {
                        if (success) {
                            Log.d("CloudinaryService", "Image deleted successfully: $publicId")
                            onResult(true, null)
                        } else {
                            Log.e("CloudinaryService", "Failed to delete image: $result")
                            onResult(false, "Failed to delete image")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CloudinaryService", "Error deleting image: ${e.message}")
                    withContext(Dispatchers.Main) {
                        onResult(false, e.message)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false, e.message)
        }
    }
}