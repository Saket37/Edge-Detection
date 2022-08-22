package com.example.edgedetectioninterview.utils

import com.bumptech.glide.load.engine.GlideException
import com.google.firebase.firestore.FirebaseFirestoreException

inline fun <T> safeCall(action: () -> Resource<T>): Resource<T> {
    return try {
        action()
    } catch (e: FirebaseFirestoreException) {
        Resource.error(null, "Unable to resolve host")
    } catch (e: GlideException) {
        Resource.error(null, "An unknown error occurred.")
    } catch (e: Exception) {
        Resource.error(null, e.localizedMessage ?: "An unknown error occurred.")
    }
}