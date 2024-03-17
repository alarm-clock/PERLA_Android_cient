package com.example.jmb_bms.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.jmb_bms.model.MenuListsHelper
import com.example.jmb_bms.model.OpenableMenuItem
import com.example.jmb_bms.model.ServerEditingIconModel
import com.example.jmb_bms.model.icons.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class TeamEditingVM(val serverVM: ServerVM,val scopeLaunch: (context: CoroutineContext, code: suspend () -> Unit) -> Unit)  {

    val model = ServerEditingIconModel(serverVM.applicationContext)

    val teamName = MutableLiveData<String>()



}