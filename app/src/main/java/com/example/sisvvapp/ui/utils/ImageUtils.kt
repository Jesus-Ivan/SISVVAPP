package com.example.sisvvapp.ui.utils

import android.content.Context
import android.util.Log
import com.example.sisvvapp.network.RetrofitClient
import java.io.File

object ImageUtils {
    fun sanitizarUrlFoto(pathOriginal: String?): String? {
        if (pathOriginal.isNullOrBlank()) {
            Log.d("IMAGE", "sanitizarUrlFoto: input es null o blank → null")
            return null
        }
        if (pathOriginal.startsWith("http") && !pathOriginal.contains("localhost")) {
            Log.d("IMAGE", "sanitizarUrlFoto: input='$pathOriginal' → output='$pathOriginal' (ya es absoluta)")
            return pathOriginal
        }

        val baseUrl = RetrofitClient.BASE_URL
            .trimEnd('/')
            .removeSuffix("/api")
            .removeSuffix("/api/")

        val rutaRelativa = pathOriginal.trimStart('/')
        val output = "$baseUrl/$rutaRelativa"
        Log.d("IMAGE", "sanitizarUrlFoto: input='$pathOriginal' → output='$output' (reconstruida)")
        return output
    }

    fun getLocalPhotoFile(context: Context, fotoUrl: String?): File? {
        if (fotoUrl.isNullOrBlank()) return null
        val file = File(context.filesDir, "photos/$fotoUrl")
        return if (file.exists()) file else null
    }
}
