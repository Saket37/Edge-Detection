package com.example.edgedetectioninterview.firebase

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.edgedetectioninterview.utils.Resource
import com.example.edgedetectioninterview.utils.safeCall
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class FirebaseRepository(private val context: Context) {
    private val firebaseStorageReference =
        Firebase.storage.reference
    private val fireStoreDatabase = FirebaseFirestore.getInstance()

    suspend fun uploadPhotos(photosUri: ArrayList<Uri>): Resource<MutableList<String>> =
        withContext(Dispatchers.IO) {
            val storageRef = Firebase.storage.reference
            val downloadUrls = mutableListOf<String>()
            safeCall {
                val uploadedPhotosUriLink =
                    (photosUri.indices).map { index ->
                        async(Dispatchers.IO) {
                            uploadPhoto(storageRef, photosUri[index])
                        }
                    }.awaitAll()

                uploadedPhotosUriLink.forEach { photoUriLink -> downloadUrls.add(photoUriLink.toString()) }
                Resource.success(downloadUrls)
            }
        }


    private suspend fun uploadPhoto(storageRef: StorageReference, photoFile: Uri): Uri? {
        val fileName = UUID.randomUUID().toString()

        return storageRef.child("images/$fileName")
            .putFile(photoFile)
            .addOnProgressListener { (bytesTransferred, totalByteCount) ->
                val progress = (100.0 * bytesTransferred) / totalByteCount
                Log.d("TAG", "Upload is $progress% done")
            }.addOnPausedListener {
                Log.d("TAG", "Upload is paused")
                Toast.makeText(context, "Upload is paused", Toast.LENGTH_SHORT).show()

            }.addOnSuccessListener {
                Toast.makeText(context, "Uploaded Successfully", Toast.LENGTH_SHORT).show()
                Log.d("Success", "Uploaded Successfully")
            }.addOnFailureListener { e ->
                e.localizedMessage?.let { Log.e("ERROR", it) }
                Toast.makeText(context, "Error uploading Images", Toast.LENGTH_SHORT).show()
                print(e.message)
            }
            .await()
            .storage
            .downloadUrl
            .await()

    }


    suspend fun uploadToFirestore(
        originalImageUri: String,
        processedImageUri: String
    ): Resource<Boolean> = withContext(Dispatchers.IO) {
        val hashMap = hashMapOf<String, Any>(
            "originalImageUri" to originalImageUri,
            "processedImageUri" to processedImageUri,
        )
        safeCall {
            fireStoreDatabase.collection("images").add(hashMap).await()
            Resource.success(true)
        }
    }

}
