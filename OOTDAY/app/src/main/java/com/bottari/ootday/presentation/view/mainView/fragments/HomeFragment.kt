package com.bottari.ootday.presentation.view.mainView.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bottari.ootday.R
import com.bottari.ootday.databinding.HomeFragmentBinding

class HomeFragment : Fragment() {

    private lateinit var binding: HomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HomeFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.plusClosetFrame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_firstClosetFragment)
        }

        binding.homeCardFrame.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_firstClosetFragment)
        }
    }
}