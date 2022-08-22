package com.example.edgedetectioninterview.utils

import android.os.Build

fun sdkCheck(): Boolean {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        return true
    }
    return false
}