package com.example.myblog.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import com.example.myblog.databinding.LoaderViewBinding

open class BaseFragment : Fragment() {


    fun showLoader(loaderBinding: LoaderViewBinding, message: String = "Loading...") {
        loaderBinding.root.visibility = View.VISIBLE
        loaderBinding.loadingTextView.text = message
    }

    // פונקציה להסתרת ה-Loader
    fun hideLoader(loaderBinding: LoaderViewBinding) {
        loaderBinding.root.visibility = View.GONE
    }
}