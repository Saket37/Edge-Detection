package com.example.edgedetectioninterview.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.edgedetectioninterview.utils.Resource
import com.example.edgedetectioninterview.firebase.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel
@Inject constructor(
    private val repository: FirebaseRepository,
) :
    ViewModel() {

    suspend fun uploadImagesToFirebase(uploadedImage: ArrayList<Uri>) = flow {
        emit(Resource.loading(null))
        try {
            val result = repository.uploadPhotos(uploadedImage)
            emit(Resource.success(result))
        } catch (e: java.lang.Exception) {
            Resource.error(null, "Unknown Error")
        }
        if (uploadedImage.toString().isEmpty()) {
            emit(Resource.error(null, "Error loading Image"))
        }

    }

    suspend fun uploadToFirestore(
        originalImageUri: String,
        processedImageUri: String
    ) = flow {
        emit(Resource.loading(null))
        try {
            val result = repository.uploadToFirestore(originalImageUri, processedImageUri)
            emit(Resource.success(result))
        } catch (E: Exception) {
            emit(Resource.error(null, "Error uploading to Database"))
        }
    }
}
