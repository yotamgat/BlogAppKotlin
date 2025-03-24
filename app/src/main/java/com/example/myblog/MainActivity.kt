package com.example.myblog

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.myblog.data.repository.AuthRepository
import com.example.myblog.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        val authRepository = AuthRepository(applicationContext)
//
//        // בדיקה: קריאה לפונקציה כדי לוודא שהמשתמש נשמר ב-Room
//        val userId = authRepository.getCurrentUserId()
//        Log.d("MainActivity", "Current User ID: $userId")
        // Find NavHostFragment and get NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Check if the user is logged in and navigate accordingly
        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is logged in, navigate to homeFragment
            navController.navigate(R.id.homeFragment)
        } else {
            // User is not logged in, navigate to loginFragment
            navController.navigate(R.id.loginFragment)
        }

        // Setup Bottom Navigation Bar with NavController
        val bottomNavView: BottomNavigationView = binding.bottomNavBar
        bottomNavView.setupWithNavController(navController)

        // Hide/Show BottomNavigationView based on the destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
            when (destination.id) {
                R.id.loginFragment -> {
                    if (isLoggedIn) {
                        // Show BottomNavigationView if user is logged in
                        binding.bottomNavBar.visibility = View.VISIBLE
                    } else {
                        // Hide BottomNavigationView if user is not logged in
                        binding.bottomNavBar.visibility = View.GONE
                    }
                }
                R.id.registerFragment -> {
                    // Hide BottomNavigationView
                    binding.bottomNavBar.visibility = View.GONE
                }
                else -> {
                    // Show BottomNavigationView
                    binding.bottomNavBar.visibility = View.VISIBLE
                }
            }
        }

        // Handle Bottom Navigation menu item selection
        bottomNavView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    // Navigate to Home if not already there
                    if (navController.currentDestination?.id != R.id.homeFragment) {
                        navController.navigate(R.id.homeFragment)
                    }
                    true
                }
                R.id.addPost -> {
                    // Navigate to Create Post
                    navController.navigate(R.id.createPostFragment)
                    true
                }
                R.id.profileFragment -> {
                    // Navigate to Profile if not already there
                    if (navController.currentDestination?.id != R.id.profileFragment) {
                        navController.navigate(R.id.profileFragment)
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}