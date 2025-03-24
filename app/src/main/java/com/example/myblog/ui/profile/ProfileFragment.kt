package com.example.myblog.ui.profile

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myblog.R
import com.example.myblog.databinding.DialogEditProfileBinding
import com.example.myblog.databinding.FragmentProfileBinding
import com.example.myblog.ui.base.BaseFragment
import com.example.myblog.ui.home.HomeViewModel
import com.example.myblog.ui.main.home.PostAdapter
import com.google.firebase.auth.FirebaseAuth

class ProfileFragment : BaseFragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val homeViewModel: HomeViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter
    private var selectedImageUri: Uri? = null

    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            editProfileDialog?.findViewById<ImageView>(com.example.myblog.R.id.profileImageView)?.let { imageView ->
                Glide.with(this).load(it).into(imageView)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            // שמירת התמונה בגלריה וטעינת URI
            val uri = MediaStore.Images.Media.insertImage(
                requireContext().contentResolver,
                bitmap,
                "ProfileImage",
                "Updated Profile Image"
            )
            selectedImageUri = Uri.parse(uri)

            Log.d("ProfileFragment", "Selected Image URI: $selectedImageUri")

            editProfileDialog?.findViewById<ImageView>(com.example.myblog.R.id.profileImageView)?.let { imageView ->
                Glide.with(this).load(selectedImageUri).into(imageView)
            }
        }
    }

    private var editProfileDialog: AlertDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSettingsIcon(view)
        profileViewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let {
                binding.profileName.text = it.name
                Glide.with(this).load(it.profileImageUrl).into(binding.profileImage)
            }
        }

        profileViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }


        profileViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }

        profileViewModel.loadProfile()

        binding.editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }
    }


    private fun setupSettingsIcon(view: View) {
        val settingsIcon = view.findViewById<ImageView>(R.id.settingsIcon)
        settingsIcon.setOnClickListener {
            showSettingsMenu(it)
        }
    }

    private fun showSettingsMenu(view: View) {
        val popupMenu = PopupMenu(requireContext(), view)
        popupMenu.inflate(R.menu.profile_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_logout -> {
                    showLogoutDialog()
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            toggleLike = { postId, liked ->
                profileViewModel.toggleLike(postId, liked)
            },
            onEditPostClicked = { post ->
                val bundle = Bundle().apply {
                    putParcelable("post", post)
                }
                findNavController().navigate(R.id.action_profileFragment_to_createPostFragment, bundle)
            },
            onDeletePostClicked = { postId ->
                showDeleteConfirmationDialog(postId)
            }
        )

        binding.postsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = postAdapter
        }
    }

    private fun showDeleteConfirmationDialog(postId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Post")
            .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
            .setPositiveButton("Yes") { _, _ ->
                deletePostWithLoader(postId) // מחיקה עם Loader
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun deletePostWithLoader(postId: String) {
        showLoader(binding.loader, "Deleting post...")
        profileViewModel.deletePost(postId) { success, error ->
            hideLoader(binding.loader)
            if (success) {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete post: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditProfileDialog() {
        val dialogBinding = DialogEditProfileBinding.inflate(LayoutInflater.from(requireContext()))

        // Prefill with current user data
        val user = profileViewModel.user.value
        user?.let {
            dialogBinding.nameEditText.setText(it.name)
            Glide.with(this).load(it.profileImageUrl).into(dialogBinding.profileImageView)
        }

        dialogBinding.profileImageView.setOnClickListener {
            val options = arrayOf("Choose from Gallery", "Take a Photo")
            AlertDialog.Builder(requireContext())
                .setTitle("Select Option")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> imagePickerLauncher.launch("image/*")
                        1 -> cameraLauncher.launch(null)
                    }
                }
                .show()
        }

        editProfileDialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .setTitle("Edit Profile")
            .setPositiveButton("Save") { _, _ ->
                val newName = dialogBinding.nameEditText.text.toString()

                if (newName.isBlank()) {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                // Show loader
                showLoader(binding.loader, "Updating profile...")
                Log.d("ProfileFragment", "Selected Image URI: $selectedImageUri")
                profileViewModel.updateProfile(newName, selectedImageUri, requireContext()) { success, message ->
                    hideLoader(binding.loader)

                    if (success) {
                        Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()

                        // עדכון הפוסטים של המשתמש עם השם והתמונה החדשים
                        val userId = profileViewModel.user.value?.id ?: return@updateProfile
                        val newProfileImageUrl = selectedImageUri?.toString() // URL של התמונה החדשה
                        profileViewModel.updateUserPosts(userId, newName, newProfileImageUrl) { postUpdateSuccess, postUpdateMessage ->
                            if (postUpdateSuccess) {
                                Log.d("ProfileFragment", "Posts updated successfully")
                            } else {
                                Log.e("ProfileFragment", "Failed to update posts: $postUpdateMessage")
                            }
                        }

                        profileViewModel.loadProfile() // Reload profile data
                    } else {
                        Toast.makeText(requireContext(), "Failed to update profile: $message", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        editProfileDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}