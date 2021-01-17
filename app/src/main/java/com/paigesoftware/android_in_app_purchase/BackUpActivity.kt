package com.paigesoftware.android_in_app_purchase

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.model.ZipParameters
import net.lingala.zip4j.model.enums.EncryptionMethod
import java.io.File
import java.io.FileOutputStream

class BackUpActivity : AppCompatActivity() {

    companion object {
        const val BACKUP_REQUEST_CODE = 204
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onLocalBackupRequested(activity: Activity) {
        val mimeTypes = arrayOf("application/zip")
        val fn = getBackupFileName("sex")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/zip")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .putExtra(
                        Intent.EXTRA_TITLE, fn
                )
        activity.startActivityForResult(intent, BACKUP_REQUEST_CODE)
    }

    fun getBackupFileName(prefix: String): String {
        return "${prefix}_sextitle"
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            resultIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultIntent)
        if (resultCode == Activity.RESULT_OK && resultIntent != null && requestCode == BACKUP_REQUEST_CODE) {
            lifecycleScope.launch {
                val uri = resultIntent.data!!
                val pfd = contentResolver.openFileDescriptor(uri, "w")
                pfd?.use {
                    FileOutputStream(pfd.fileDescriptor).use { outputStream ->
                        val file = packZipFileForBackup(this@BackUpActivity)
                        println("filepath: ${file?.absolutePath}")
                        try {
                            file?.inputStream()?.use { input ->
                                input.copyTo(outputStream)
                            }

                        } finally {
                            if (file?.exists() == true) {
                                file.delete()
                            }
                        }
                    }
                }
            }
        }
    }

    suspend fun packZipFileForBackup(context: Context): File? {
        return withContext(Dispatchers.IO) {
            val dbFile = context.getDatabasePath("dbName.db") // replace this with your db name
            val dbParentDirectory = dbFile.parentFile
            val zipFilePath = context.filesDir.path + "/backup.zip" // create zip file for backup
            val zipFile = File(zipFilePath)

            val dataDir = context.filesDir.parentFile
            if (dataDir != null) {
                val sharedPrefDirectoryPath = dataDir.absolutePath + "/shared_prefs"
                val encZipFile = ZipFile(zipFile.absolutePath, "password".toCharArray())
                val zipParameters = ZipParameters()
                zipParameters.isEncryptFiles = true
                zipParameters.encryptionMethod = EncryptionMethod.AES
                encZipFile.addFolder(File(sharedPrefDirectoryPath), zipParameters) // add shared pref directory
                encZipFile.addFolder(context.filesDir, zipParameters) // add files directory
                encZipFile.addFolder(dbParentDirectory, zipParameters) // add database directory
            }
            return@withContext zipFile
        }
    }

}


