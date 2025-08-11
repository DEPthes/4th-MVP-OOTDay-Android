package com.bottari.ootday.presentation.view.mainView.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bottari.ootday.databinding.SecondClosetPictureFragmentBinding
import com.bumptech.glide.Glide

class SecondClosetPictureFragment : Fragment() {

    private var _binding: SecondClosetPictureFragmentBinding? = null
    private val binding get() = _binding!!

    private val args: SecondClosetPictureFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = SecondClosetPictureFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUri = Uri.parse(args.imageUri)

        Glide.with(this)
            .load(imageUri)
            .into(binding.myPicture)

        binding.findMyItemButton.setOnClickListener {
            val action = SecondClosetPictureFragmentDirections.actionSecondClosetPictureFragmentToSecondClosetLoadingFragment()
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}