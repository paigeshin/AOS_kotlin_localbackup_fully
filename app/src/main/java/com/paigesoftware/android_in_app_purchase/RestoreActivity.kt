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
import net.lingala.zip4j.exception.ZipException
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

class RestoreActivity : AppCompatActivity() {

    companion object {
        const val RESTORE_REQUEST_CODE = 502
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restore)
    }

    fun onLocalRestoreRequested(activity: Activity) {
        val mimeTypes = arrayOf("application/zip", "application/octet-stream", "application/x-zip-compressed", "multipart/x-zip")
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("*/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
            .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        activity.startActivityForResult(intent, RESTORE_REQUEST_CODE)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        resultIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, resultIntent)
        if (resultCode == Activity.RESULT_OK && resultIntent != null && requestCode == RESTORE_REQUEST_CODE) {
            lifecycleScope.launch {
                val uri = resultIntent.data!!
                contentResolver
                    .takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                val pfd = contentResolver.openFileDescriptor(uri, "r")
                pfd?.use {
                    FileInputStream(pfd.fileDescriptor).use { inputStream ->
                        try {
                            val restoreResult = restoreFromInputStream(
                                this@RestoreActivity,
                                inputStream
                            )
                            if (restoreResult) {
                                // Success action
                            } else {
                                //failure action
                            }
                        } catch (e: ZipException) {
                            // incorrect password
                        }
                    }
                }
            }
        }
    }

    suspend fun restoreFromInputStream(context: Context,
                                       contentStream: InputStream
    ): Boolean {
        return withContext(Dispatchers.IO) {
            var result = false
            var toBeRestoredZipFile: File? = null
            var extractedFilesDir: File? = null
            try {
                val dbFile = context.getDatabasePath("dbName.db")
                val parentDbFile = dbFile.parentFile
                val dataDir = requireNotNull(context.filesDir.parentFile)

                toBeRestoredZipFile = File(dataDir.absolutePath + "/toBeRestored.zip") //zip file that is going to be restored
                extractedFilesDir = File(dataDir.absolutePath + "/toBeRestoredDir") // directory used to temporary extract all files
                extractedFilesDir.mkdir()
                contentStream.use { input ->     //Copy stream into temporary file
                    toBeRestoredZipFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val preparedZipFile =
                    ZipFile(toBeRestoredZipFile.absolutePath, getFinalZipPass("password").toCharArray())
                preparedZipFile.extractAll(extractedFilesDir.absolutePath) // extract all the files inside the zip file

                //delete existing data directories. these will be replaced with the ones in zip file
                val sharedPrefDirePath = dataDir.absolutePath + "/shared_prefs"
                val sharedPrefDir = File(sharedPrefDirePath)
                sharedPrefDir.deleteRecursively()

                parentDbFile?.listFiles()
                    ?.forEach {
                        it.deleteRecursively()
                    }

                context.filesDir.listFiles()?.forEach {
                    it.deleteRecursively()
                }


                // copy all the files that were extracted from zip and place them under `data` directory
                if (extractedFilesDir!!.exists()) {
                    val toBeRestoredFolders = extractedFilesDir.listFiles()
                    toBeRestoredFolders?.forEach {
                        val contentFolderInData = File(dataDir.absolutePath + "/" + it.name)
                        if (!contentFolderInData.exists()) {
                            contentFolderInData.mkdir()
                        }
                        it.copyRecursively(contentFolderInData)
                    }
                    result = true
                }
            } catch (e: ZipException) {
                if (e.type == ZipException.Type.WRONG_PASSWORD) {
                    // Provided password is wrong for the zip hence it can not be extracted
                    throw ZipException("Invalid password", e)
                }
                throw e
            } finally {
                if (extractedFilesDir?.exists() == true) {
                    extractedFilesDir.deleteRecursively() // delete directory used to extract all files
                }

                if (toBeRestoredZipFile?.exists() == true) {
                    toBeRestoredZipFile.delete() // delete actual .zip file that was restored
                }
            }
            return@withContext result
        }
    }

    fun getFinalZipPass(password: String): String {
        return password
    }

}