package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bottari.ootday.databinding.ProfileClosetFragmentBinding

class ProfileClosetFragment : Fragment() {
    private var _binding: ProfileClosetFragmentBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ):  View {
        _binding = ProfileClosetFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
    }

    private fun setupClickListeners() {


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
