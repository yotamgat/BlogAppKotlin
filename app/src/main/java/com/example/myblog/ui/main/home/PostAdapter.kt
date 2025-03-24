package com.example.myblog.ui.main.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myblog.R
import com.example.myblog.data.model.Post
import com.google.firebase.auth.FirebaseAuth

class PostAdapter(
    private val toggleLike: (String, Boolean) -> Unit,
    private val onEditPostClicked: (Post) -> Unit ,
    private val onDeletePostClicked: (String) -> Unit
) : ListAdapter<Post, PostAdapter.PostViewHolder>(PostDiffCallback()) {

    inner class PostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profileImageView)
        val userName: TextView = itemView.findViewById(R.id.userNameTextView)
        val postImage: ImageView = itemView.findViewById(R.id.postImageView)
        val postDescription: TextView = itemView.findViewById(R.id.postDescriptionTextView)
        val likeIcon: ImageView = itemView.findViewById(R.id.likeIcon)
        val likeCount: TextView = itemView.findViewById(R.id.likeCount)

        val menuButton: ImageButton = itemView.findViewById(R.id.menuButton) // כפתור שלוש נקודות
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)

        holder.userName.text = post.userName
        holder.postDescription.text = post.description
        holder.likeCount.text = post.likes.size.toString()


        Glide.with(holder.itemView.context).load(post.userProfileImageUrl).into(holder.profileImage)
        Glide.with(holder.itemView.context).load(post.postImageUrl).into(holder.postImage)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val userLiked = currentUserId != null && post.likes.contains(currentUserId)

        holder.likeIcon.isSelected = userLiked


        holder.likeIcon.setOnClickListener {
            if (currentUserId == null) {
                Toast.makeText(holder.itemView.context, "You must be logged in to like posts.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val liked = !userLiked
            toggleLike(post.id, liked)


            val message = if (liked) {
                "You liked ${post.userName}'s post!"
            } else {
                "You unliked ${post.userName}'s post!"
            }
            Toast.makeText(holder.itemView.context, message, Toast.LENGTH_SHORT).show()
        }


        if (post.userId == currentUserId) {
            holder.menuButton.visibility = View.VISIBLE
            holder.menuButton.setOnClickListener {
                showPopupMenu(holder.menuButton, post)
            }
        } else {
            holder.menuButton.visibility = View.GONE
        }
    }

    private fun showPopupMenu(view: View, post: Post) {
        val popupMenu = PopupMenu(view.context, view)
        popupMenu.inflate(R.menu.post_menu)

        try {
            val fields = popupMenu.javaClass.declaredFields
            for (field in fields) {
                if ("mPopup" == field.name) {
                    field.isAccessible = true
                    val menuPopupHelper = field.get(popupMenu)
                    val classPopupHelper = Class.forName(menuPopupHelper.javaClass.name)
                    val setForceIcons = classPopupHelper.getMethod("setForceShowIcon", Boolean::class.java)
                    setForceIcons.invoke(menuPopupHelper, true)
                    break
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_post -> {
                    onEditPostClicked(post)
                    true
                }
                R.id.delete_post -> {
                    onDeletePostClicked(post.id)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem == newItem
        }
    }
}