package com.example.jmb_bms.model.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext


fun ViewModel.runOnThread(coroutineContext: CoroutineContext, runnable: suspend () -> Unit)
{
    viewModelScope.launch {
        withContext(coroutineContext){
            runnable()
        }
    }
}