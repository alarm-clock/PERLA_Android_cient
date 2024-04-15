package com.example.jmb_bms.model.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import locus.api.android.utils.LocusConst
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

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

    private fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDirectory = context.filesDir //context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)  //

        //File(context.filesDir,"images/JMB_BMS_JPG_$timeStamp.jpg")

        return File.createTempFile(
            "JMB_BMS_JPG_${timeStamp}_", ".jpg", storageDirectory
        ).apply {
            currentPhotoPath = absolutePath
        }
    }
}