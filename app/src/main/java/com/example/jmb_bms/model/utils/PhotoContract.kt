/**
 * @file: PhotoContract.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PhotoContract class
 */
package com.example.jmb_bms.model.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Class that extends [ActivityResultContract] and starts photo capture activity and later parses result from it. In the
 * eye of user it simply creates file in which is photo stored and returns its [Uri]
 */
class PhotoContract: ActivityResultContract<Unit, Uri?>() {

    private var currentPhotoPath: String? = null
    private var photoURI: Uri? = null
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            resolveActivity(context.packageManager)?.also {
                val photoFile = createImageFile(context)
                photoURI = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    photoFile
                )
                putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                context.grantUriPermission("menion.android.locus",photoURI,Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }
    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        if(resultCode == Activity.RESULT_OK)
        {
            return photoURI
        }
        return null
    }

    /**
     * Method that creates image file into which captured photo will be stored
     * @param context Context used to create file
     * @return [File] in which photo will be stored
     */
    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDirectory = context.filesDir

        return File.createTempFile(
            "JMB_BMS_JPG_${timeStamp}_", ".jpg", storageDirectory
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}