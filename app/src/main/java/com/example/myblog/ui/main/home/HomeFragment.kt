package com.example.myblog.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myblog.R
import com.example.myblog.databinding.FragmentHomeBinding
import com.example.myblog.ui.base.BaseFragment


import com.example.myblog.ui.main.home.PostAdapter

class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val homeViewModel: HomeViewModel by viewModels()
    private lateinit var postAdapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()


        homeViewModel.posts.observe(viewLifecycleOwner) { posts ->
            postAdapter.submitList(posts)
        }


        homeViewModel.loadPosts()
    }

    private fun setupRecyclerView() {
        postAdapter = PostAdapter(
            toggleLike = { postId, liked ->
                homeViewModel.toggleLike(postId, liked)
            },
            onEditPostClicked = { post ->

                val bundle = Bundle().apply {
                    putParcelable("post", post)
                }

                findNavController().navigate(R.id.action_homeFragment_to_createPostFragment, bundle)
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
        homeViewModel.deletePost(postId) { success, error ->
            hideLoader(binding.loader)
            if (success) {
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Failed to delete post: $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}