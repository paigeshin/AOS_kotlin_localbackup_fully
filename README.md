- [https://github.com/srikanth-lingala/zip4j](https://github.com/srikanth-lingala/zip4j)
- [https://medium.com/@spians/exploring-how-to-provide-your-own-backup-restore-mechanism-in-your-android-app-99e5885ff7ed](https://medium.com/@spians/exploring-how-to-provide-your-own-backup-restore-mechanism-in-your-android-app-99e5885ff7ed)
- You can upload zip file to google drive or dropbox

# Dependency - on app level

```kotlin
def coroutine_version = "1.4.2"
def lifecycle_version = "2.2.0"

implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
implementation 'androidx.core:core-ktx:1.3.2'
implementation 'androidx.appcompat:appcompat:1.2.0'
implementation 'com.google.android.material:material:1.2.1'
implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
testImplementation 'junit:junit:4.+'
androidTestImplementation 'androidx.test.ext:junit:1.1.2'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine_version"

// ViewModel & LiveData & lifecycle
implementation "androidx.lifecycle:lifecycle-extensions:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"

implementation "net.lingala.zip4j:zip4j:2.6.4"
```

# BackUp

```kotlin
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
```

# Restore

```kotlin
- [https://github.com/srikanth-lingala/zip4j](https://github.com/srikanth-lingala/zip4j)
- [https://medium.com/@spians/exploring-how-to-provide-your-own-backup-restore-mechanism-in-your-android-app-99e5885ff7ed](https://medium.com/@spians/exploring-how-to-provide-your-own-backup-restore-mechanism-in-your-android-app-99e5885ff7ed)
- You can upload zip file to google drive or dropbox

# Dependency - on app level

```kotlin
def coroutine_version = "1.4.2"
def lifecycle_version = "2.2.0"

implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
implementation 'androidx.core:core-ktx:1.3.2'
implementation 'androidx.appcompat:appcompat:1.2.0'
implementation 'com.google.android.material:material:1.2.1'
implementation 'androidx.constraintlayout:constraintlayout:2.0.4'
testImplementation 'junit:junit:4.+'
androidTestImplementation 'androidx.test.ext:junit:1.1.2'
androidTestImplementation 'androidx.test.espresso:espresso-core:3.3.0'

// Coroutines
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutine_version"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutine_version"

// ViewModel & LiveData & lifecycle
implementation "androidx.lifecycle:lifecycle-extensions:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-livedata-ktx:$lifecycle_version"
implementation "androidx.lifecycle:lifecycle-runtime-ktx:$lifecycle_version"

implementation "net.lingala.zip4j:zip4j:2.6.4"
```

# BackUp

```kotlin
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
```

# Restore

```kotlin
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
```
