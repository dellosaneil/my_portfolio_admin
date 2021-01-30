package com.example.portfolioadmin.project

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.portfolioadmin.Constants.Companion.CHECK_UPDATE
import com.example.portfolioadmin.Constants.Companion.MAIN_COLLECTION
import com.example.portfolioadmin.Constants.Companion.MAIN_DOCUMENT
import com.example.portfolioadmin.Constants.Companion.PROJECT_IMAGE_PATH
import com.example.portfolioadmin.Constants.Companion.PROJECT_PATH
import com.example.portfolioadmin.Constants.Companion.UPDATE
import com.example.portfolioadmin.R
import com.example.portfolioadmin.databinding.FragmentProjectBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File


class ProjectFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentProjectBinding? = null
    private val binding get() = _binding!!
    private var resourceClicked: Int = 0
    private val storage = Firebase.storage.reference
    private val firestore = Firebase.firestore.collection(MAIN_COLLECTION).document(MAIN_DOCUMENT)

    private var firstImageUri: Uri? = null
    private var secondImageUri: Uri? = null
    private var thirdImageUri: Uri? = null
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProjectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.projectFirstImage.setOnClickListener(this)
        binding.projectSecondImage.setOnClickListener(this)
        binding.projectThirdImage.setOnClickListener(this)
        binding.projectSubmit.setOnClickListener(this)

    }

    private fun getImage(resourceId: Int) {
        Intent(Intent.ACTION_GET_CONTENT).also {
            it.type = "image/*"
            resourceClicked = assignId(resourceId)
            startActivityForResult(it, resourceClicked)
        }
    }

    private fun assignId(resourceId: Int): Int {
        return when (resourceId) {
            R.id.project_firstImage -> 1
            R.id.project_secondImage -> 2
            R.id.project_thirdImage -> 3
            else -> 4
        }
    }
    private val TAG = "ProjectFragment"

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == resourceClicked) {
            data?.data?.let {
                placeImage(resourceClicked, it)
            }
        }
    }

    private fun placeImage(id: Int, uri: Uri) {
        when (id) {
            1 -> {
                firstImageUri = uri
                binding.projectFirstImage.setImageURI(uri)
            }
            2 -> {
                secondImageUri = uri
                binding.projectSecondImage.setImageURI(uri)
            }
            3 -> {
                thirdImageUri = uri
                binding.projectThirdImage.setImageURI(uri)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.project_firstImage -> getImage(R.id.project_firstImage)
            R.id.project_secondImage -> getImage(R.id.project_secondImage)
            R.id.project_thirdImage -> getImage(R.id.project_thirdImage)
            R.id.project_submit -> {
                uploadToStorage()
            }
        }
    }

    private fun uploadToStorage() {
        if (checkValues()) {
            clearErrors()
            val name = binding.projectName.editText?.text.toString()
            val description = binding.projectDescription.editText?.text.toString()
            val language = binding.projectLanguage.editText?.text.toString()
            val github = binding.projectGithub.editText?.text.toString()
            val firstImage = "$PROJECT_IMAGE_PATH/$name/first"
            val secondImage = "$PROJECT_IMAGE_PATH/$name/second"
            val thirdImage = "$PROJECT_IMAGE_PATH/$name/third"
            saveToFireStore(
                name,
                description,
                language,
                github,
                firstImage,
                secondImage,
                thirdImage
            )
        }
    }

    private fun saveToFireStore(
        name: String,
        description: String,
        language: String,
        github: String,
        firstImage: String,
        secondImage: String,
        thirdImage: String
    ) {
        val temp =
            ProjectData(name, description, language, github, firstImage, secondImage, thirdImage)
        lifecycleScope.launch(Dispatchers.IO) {
            if (!checkProject(name)) {
                firestore.collection(PROJECT_PATH).document(name).set(temp)
                placeInStorage(firstImage, secondImage, thirdImage)
            } else {
                withContext(Dispatchers.Main) {
                    binding.projectName.error = getString(R.string.certification_check)
                }
            }
        }
    }

    private suspend fun placeInStorage(
        firstImage: String,
        secondImage: String,
        thirdImage: String
    ) {
        Log.i(TAG, "URI: $firstImageUri")
        Log.i(TAG, "IMAGE REFERENCE: $firstImage")
        firstImageUri?.let { storage.child(firstImage).putFile(it) }
        secondImageUri?.let { storage.child(secondImage).putFile(it) }
        thirdImageUri?.let { storage.child(thirdImage).putFile(it) }
        clearValues()
    }

    private suspend fun clearValues() {
        val temp = mapOf(UPDATE to true)
        firestore.collection(CHECK_UPDATE).document(PROJECT_PATH).set(temp)
        withContext(Dispatchers.Main) {
            binding.projectName.editText?.text?.clear()
            binding.projectDescription.editText?.text?.clear()
            binding.projectLanguage.editText?.text?.clear()
            binding.projectGithub.editText?.text?.clear()
            binding.projectFirstImage.setImageURI(null)
            binding.projectSecondImage.setImageURI(null)
            binding.projectThirdImage.setImageURI(null)
            Toast.makeText(
                requireContext(),
                getString(R.string.portfolio_updated),
                Toast.LENGTH_LONG
            ).show()
        }
    }


    private suspend fun checkProject(name: String): Boolean {
        var check = false
        firestore.collection(PROJECT_PATH).document(name).get().addOnCompleteListener { snapshot ->
            run {
                check = snapshot.result?.exists() ?: true
            }
        }.await()
        return check
    }


    private fun clearErrors() {
        binding.projectName.error = null
        binding.projectDescription.error = null
        binding.projectLanguage.error = null
        binding.projectGithub.error = null
    }

    private fun checkValues(): Boolean {
        clearErrors()
        when {
            binding.projectName.editText?.text?.isEmpty() == true -> {
                binding.projectName.error = resources.getString(R.string.required_field)
                return false
            }
            binding.projectDescription.editText?.text?.isEmpty() == true -> {
                binding.projectDescription.error = resources.getString(R.string.required_field)
                return false
            }
            binding.projectLanguage.editText?.text?.isEmpty() == true -> {
                binding.projectLanguage.error = resources.getString(R.string.required_field)
                return false
            }
            binding.projectGithub.editText?.text?.isEmpty() == true -> {
                binding.projectGithub.error = resources.getString(R.string.required_field)
                return false
            }
            binding.projectFirstImage.drawable == null -> {
                return false
            }
            binding.projectSecondImage.drawable == null -> {
                binding.projectName.error = resources.getString(R.string.required_field)
                return false
            }
            binding.projectThirdImage.drawable == null -> {
                return false
            }
            else -> return true
        }
    }

}