package com.example.jmb_bms.model.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoContract : ActivityResultContract<Unit, Uri?>() {

    private lateinit var ctx: Context
    private var name: String? = null
    private var fileName: String? = null
    private var videoURI: Uri? = null
    private val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    override fun createIntent(context: Context, input: Unit): Intent {
        ctx = context
        return Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
            resolveActivity(context.packageManager)?.also {
                val videoFile = createVideoFile(context)
                videoURI = FileProvider.getUriForFile(
                    context,
                    context.packageName + ".provider",
                    videoFile
                )
                putExtra(MediaStore.EXTRA_OUTPUT, videoURI)
            }
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? {

        if(resultCode == Activity.RESULT_OK && videoURI != null)
        {
            val mimeType = getVideoFormat()

            return if(renameFile(mimeType)) videoURI else null
        }

        return null
    }

    private fun renameFile(mime: String): Boolean
    {
        val oldFile = File(ctx.filesDir, name!!)

        name = name!!.replace("TMP_","").replace(".tmpvideo","")
        val newFile = File(ctx.filesDir,"$name.${mime.takeLastWhile{ it != '/' }}")
        val res = oldFile.renameTo(newFile)

        if(!res) return false

        videoURI = FileProvider.getUriForFile(
            ctx,
            ctx.packageName + ".provider",
            newFile
        )
        ctx.grantUriPermission("menion.android.locus",videoURI,Intent.FLAG_GRANT_READ_URI_PERMISSION)

        return true
    }

    private fun getVideoFormat(): String {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(ctx, videoURI)
        val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        retriever.release()
        return mimeType ?: "unknown"
    }

    private fun createVideoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDirectory = context.filesDir

        fileName = "JMB_BMS_TMP_VIDEO_${timeStamp}_"

        return File.createTempFile(
            fileName!!, ".tmpvideo", storageDirectory
        ).apply {
            this@VideoContract.name = this.name
        }
    }
}