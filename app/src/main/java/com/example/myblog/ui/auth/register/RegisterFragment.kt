package com.example.myblog.ui.auth.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.myblog.R
import com.example.myblog.databinding.FragmentRegisterBinding
import com.example.myblog.ui.base.BaseFragment

class RegisterFragment : BaseFragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val registerViewModel: RegisterViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.registerButton.setOnClickListener {
            val name = binding.nameEditText.text.toString()
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            if (validateInput(name, email, password, confirmPassword)) {
                showLoader(binding.loader, "Registering...")

                registerViewModel.registerUser(name, email, password) { success, message ->
                    hideLoader(binding.loader)
                    if (success) {
                        Toast.makeText(requireContext(), "User registered successfully!", Toast.LENGTH_SHORT).show()
                        // מעבר ל-HomeFragment לאחר רישום מוצלח
                        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                    } else {
                        showError(message ?: "Registration failed")
                    }
                }
            }
        }

        binding.alreadyHaveAccount.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        return when {
            name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                showError("All fields are required")
                false
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                false
            }
            else -> true
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}