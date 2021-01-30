package com.example.portfolioadmin.certification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.portfolioadmin.Constants
import com.example.portfolioadmin.Constants.Companion.CERTIFICATION_PATH
import com.example.portfolioadmin.Constants.Companion.CHECK_UPDATE
import com.example.portfolioadmin.Constants.Companion.UPDATE
import com.example.portfolioadmin.R
import com.example.portfolioadmin.databinding.FragmentCertificationBinding
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CertificationFragment : Fragment() {

    private var _binding: FragmentCertificationBinding? = null
    private val binding get() = _binding!!
    private var certificationReference =
        Firebase.firestore.collection(Constants.MAIN_COLLECTION).document(
            Constants.MAIN_DOCUMENT
        )


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCertificationBinding.inflate(inflater, container, false)
        binding.certificationSubmit.setOnClickListener {
            insertCertificate()
        }

        return binding.root
    }

    private fun insertCertificate() {
        val certificateTitle = binding.certificationTitle.editText?.text.toString()
        val certificateCompany = binding.certificationCompany.editText?.text.toString()
        val credentialID = binding.certificationId.editText?.text.toString()
        val certificateLink = binding.certificationUrl.editText?.text.toString()
        errorMessages(certificateTitle, certificateCompany, credentialID, certificateLink)
    }

    private fun errorMessages(
        certificateTitle: String,
        certificateCompany: String,
        credentialID: String,
        certificateLink: String
    ) {
        var testCertificate = true
        if (certificateTitle.isEmpty()) {
            binding.certificationTitle.error = resources.getString(R.string.required_field)
            testCertificate = false
        } else {
            binding.certificationTitle.error = null
        }
        if (certificateCompany.isEmpty()) {
            binding.certificationCompany.error = resources.getString(R.string.required_field)
            testCertificate = false
        } else {
            binding.certificationCompany.error = null
        }
        if (credentialID.isEmpty()) {
            binding.certificationId.error = resources.getString(R.string.required_field)
            testCertificate = false
        } else {
            binding.certificationId.error = null
        }
        if (certificateLink.isEmpty()) {
            binding.certificationUrl.error = resources.getString(R.string.required_field)
            testCertificate = false
        } else {
            binding.certificationUrl.error = null
        }
        if (testCertificate) {
            checkCertificate(certificateTitle, certificateCompany, credentialID, certificateLink)
        }
    }

    private fun checkCertificate(
        certificateTitle: String,
        certificateCompany: String,
        credentialID: String,
        certificateLink: String
    ) {
        val certificationDocument =
            certificationReference.collection(CERTIFICATION_PATH).document(credentialID).get()
        certificationDocument.addOnSuccessListener { document ->
            val certificate = document.toObject(CertificateData::class.java)
            if (certificate == null) {
                val temp =
                    CertificateData(
                        certificateTitle,
                        certificateCompany,
                        credentialID,
                        certificateLink
                    )
                certificationReference.collection(CERTIFICATION_PATH).document(credentialID)
                    .set(temp)
                clearEditText()
                checkUpdate()
                Toast.makeText(requireContext(), "Certification Saved", Toast.LENGTH_LONG).show()

            } else {
                binding.certificationId.error = resources.getString(R.string.certification_check)
            }
        }
    }

    private fun clearEditText() {
        binding.certificationTitle.editText?.text?.clear()
        binding.certificationCompany.editText?.text?.clear()
        binding.certificationUrl.editText?.text?.clear()
        binding.certificationId.editText?.text?.clear()
    }

    private fun checkUpdate() {
        val temp = mapOf(UPDATE to true)
        certificationReference.collection(CHECK_UPDATE).document(CERTIFICATION_PATH).set(temp)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}