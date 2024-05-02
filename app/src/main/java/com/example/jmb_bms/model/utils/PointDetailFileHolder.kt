package com.example.jmb_bms.model.utils

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class PointDetailFileHolder(
    val uri: Uri,
    val loadingState: MutableLiveData<Int?>?,
    val mimeType: MimeTypes
    )