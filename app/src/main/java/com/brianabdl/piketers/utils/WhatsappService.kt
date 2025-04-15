package com.brianabdl.piketers.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

object WhatsappService {

    suspend fun sendMessage(
        context: Context,
        text: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (!isWhatsappInstalled(context.packageManager)) {
            return@withContext Result.failure(IOException("Whatsapp Not Installed!"));
        }

        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
            setPackage("com.whatsapp") // This ensures it opens WhatsApp specifically
        }

        try {
            context.startActivity(intent)
            return@withContext Result.success("Message sent successfully!")
        } catch (_: ActivityNotFoundException) {
        }
        return@withContext Result.failure(IOException("Unable to sent message!"))
    }

    private fun isWhatsappInstalled(pkgMgr: PackageManager): Boolean {
        try {
            pkgMgr.getPackageInfo("com.whatsapp", 0)
            return true
        } catch (e: Exception) {
            return false
        }
    }
}