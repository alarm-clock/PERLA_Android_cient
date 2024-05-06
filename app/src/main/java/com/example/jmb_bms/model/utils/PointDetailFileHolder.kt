/**
 * @file: PointDetailFileHolder.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing PointDetailFileHolder class
 */
package com.example.jmb_bms.model.utils

import android.net.Uri
import androidx.lifecycle.MutableLiveData

/**
 * Class that holds file [Uri], downloading percentage, and [MimeTypes]
 * @param uri File [Uri]
 * @param loadingState Loading percentage
 * @param mimeType Files mimeType
 */
class PointDetailFileHolder(
    val uri: Uri,
    val loadingState: MutableLiveData<Int?>?,
    val mimeType: MimeTypes
    )