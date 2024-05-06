/**
 * @file: UriExtension.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing extension methods for obtaining file size and original name from Uri
 */
package com.example.jmb_bms.model.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.FileNotFoundException

/**
 * Extension method of [Uri] class that gets original file identified by uri from contentResolver database
 * @param context Context used to query content resolver
 * @return File name or null if error occurred
 */
fun Uri.getOriginalFileName(context: Context): String? {
    return context.contentResolver.query(this, null, null, null, null)?.use {
        val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        it.moveToFirst()
        it.getString(nameColumnIndex)
    }
}

/**
 * Extension method that returns size of file identified by uri
 * @param contentResolver Content resolver that will be used to obtain file or query its database
 * @return File size or -1 if error occurred
 */
fun Uri.getFileSize(contentResolver: ContentResolver): Long {

    //get files file descriptor in reading mode
    val assetFileDescriptor = try {
        contentResolver.openAssetFileDescriptor(this, "r")
    } catch (e: FileNotFoundException) {
        null
    }
    // uses db query and size column underneath if failed
    val length = assetFileDescriptor?.use { it.length } ?: -1L
    if (length != -1L) {
        return length
    }

    // if file has "content://" uri scheme then we can try to query contResolv db and look into SIZE column
    if (scheme.equals(ContentResolver.SCHEME_CONTENT)) {
        return contentResolver.query(this, arrayOf(OpenableColumns.SIZE), null, null, null)
            ?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex == -1) {
                    return@use -1L
                }
                cursor.moveToFirst()
                return try {
                    cursor.getLong(sizeIndex)
                } catch (_: Throwable) {
                    -1L
                }
            } ?: -1L
    } else {
        return -1L
    }
}