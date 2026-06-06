package com.zando.app.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64

fun getBase64Bitmap(base64String: String?): Bitmap? {
    if (base64String == null) return null
    return try {
        // Remove the "data:image/jpeg;base64," prefix if it exists in the string
        val cleanString = if (base64String.contains(",")) {
            base64String.substring(base64String.indexOf(",") + 1)
        } else {
            base64String
        }
        val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        null
    }
}
