package com.example.kessekolah.ui.core.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.kessekolah.MainActivity
import com.example.kessekolah.R
import com.example.kessekolah.data.remote.LoginData
import com.example.kessekolah.databinding.FragmentProfileBinding
import com.example.kessekolah.utils.LoginPreference
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var preference: LoginPreference
    private lateinit var dataLogin: LoginData
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preference = LoginPreference(requireContext())
        dataLogin = preference.getData()
        storage = FirebaseStorage.getInstance()

        setupData()
        buttonClick()

        parentFragmentManager.setFragmentResultListener("editProfileResult", viewLifecycleOwner) { _, result ->
            val updatedName = result.getString("updatedName")
            val updatedProfilePicture = result.getString("updatedProfilePicture")

            updatedName?.let { dataLogin.name = it }
            updatedProfilePicture?.let { dataLogin.profilePicture = it }

            setupData()
        }
    }

    private fun setupData() = with(binding) {
        tvName.text = dataLogin.name
        tvEmail.text = dataLogin.email
        tvProfesi.text = dataLogin.role

        if (dataLogin.profilePicture?.isNotEmpty() == true) {
            Picasso.get().load(dataLogin.profilePicture).into(imgAvatar)
        } else {
            imgAvatar.setImageResource(R.drawable.logo_app)
        }
    }


    private fun buttonClick() = with(binding) {
        btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }

        btnTentangSekolah.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_tentangSekolahFragment)
        }

        btnKeluar.setOnClickListener {
            preference.removeData()

            findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
