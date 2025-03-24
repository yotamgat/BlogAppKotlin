package com.example.myblog.ui.post.create

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.myblog.R
import com.example.myblog.data.model.Post
import com.example.myblog.databinding.FragmentCreatePostBinding
import com.example.myblog.ui.base.BaseFragment
import kotlinx.coroutines.launch

class CreatePostFragment : BaseFragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!
    private val createPostViewModel: CreatePostViewModel by viewModels()
    private var selectedImageUri: Uri? = null

    // Launcher לבחירת תמונה מהגלריה
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            Glide.with(this).load(it).into(binding.imagePreview)
        }
    }

    // Launcher לצילום תמונה במצלמה
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
        bitmap?.let {
            // שמירת התמונה בגלריה וקבלת ה-URI
            val uri = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                it,
                "CapturedImage",
                "Image captured by camera"
            )
            selectedImageUri = Uri.parse(uri)
            Glide.with(this).load(selectedImageUri).into(binding.imagePreview)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val post: Post? = arguments?.getParcelable("post")

        // שינוי כותרת וכפתור בהתאם למצב (עדכון או יצירת פוסט חדש)
        if (post != null) {
            binding.toolbar.title = "Update Post"
            binding.uploadPostButton.text = "Update Post"
            Glide.with(this).load(post.postImageUrl).into(binding.imagePreview)
            binding.descriptionEditText.setText(post.description)
            selectedImageUri = null // כדי לבדוק אם שונו הנתונים
        } else {
            binding.toolbar.title = "Create Post"
            binding.uploadPostButton.text = "Upload Post"
        }

        // בחירת תמונה או מצלמה
        binding.selectImageButton.setOnClickListener {
            showImageSourceDialog()
        }

        binding.aiGenerateButton.setOnClickListener {
            createPostViewModel.generateDescription { generatedText ->
                binding.descriptionEditText.setText(generatedText)
            }
        }

        binding.uploadPostButton.setOnClickListener {
            handleUploadOrUpdate(post)
        }
    }

    // מציג דיאלוג לבחירה בין מצלמה לגלריה
    private fun showImageSourceDialog() {
        val options = arrayOf("Choose from Gallery", "Take a Photo")
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Select Image Source")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> imagePickerLauncher.launch("image/*") // גלריה
                    1 -> cameraLauncher.launch(null) // מצלמה
                }
            }
            .show()
    }

    private fun handleUploadOrUpdate(post: Post?) {
        val description = binding.descriptionEditText.text.toString()

        if (description.isEmpty()) {
            showToast(requireContext(), "Please write a description")
            return
        }

        showLoader(binding.loader, if (post != null) "Updating post..." else "Uploading post...")

        lifecycleScope.launch {
            if (post != null) {
                // אם זו עריכה, נבדוק אם המשתמש שינה את התמונה
                if (selectedImageUri != null) {
                    // עדכון תמונה ותיאור
                    createPostViewModel.updatePostWithImage(
                        postId = post.id,
                        imageUri = selectedImageUri!!,
                        description = description,
                        context = requireContext()
                    ) { success, message ->
                        handleResult(success, message)
                    }
                } else {
                    // עדכון רק תיאור
                    createPostViewModel.updatePostDescription(
                        postId = post.id,
                        description = description
                    ) { success, message ->
                        handleResult(success, message)
                    }
                }
            } else {
                // יצירת פוסט חדש
                if (selectedImageUri == null) {
                    showToast(requireContext(), "Please select an image")
                    return@launch
                }

                createPostViewModel.uploadPost(
                    selectedImageUri!!,
                    description,
                    requireContext()
                ) { success, message ->
                    handleResult(success, message)
                }
            }
        }
    }

    private fun handleResult(success: Boolean, message: String?) {
        hideLoader(binding.loader)
        if (success) {
            showToast(requireContext(), "Post saved successfully")
            findNavController().navigate(R.id.action_createPostFragment_to_homeFragment)
        } else {
            showToast(requireContext(), "Error: $message")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}