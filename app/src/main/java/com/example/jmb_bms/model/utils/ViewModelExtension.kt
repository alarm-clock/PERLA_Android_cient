/**
 * @file: ViewModelExtension.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing runOnThread extension method for ViewModel class
 */
package com.example.jmb_bms.model.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

/**
 * Method that launches [runnable] on coroutine that is aware of [ViewModel]s lifecycle. Written in time in which I didn't
 * know that launch method can directly take coroutine. It can be seen that I was learning this language on fly so please
 * don't judge
 */
fun ViewModel.runOnThread(coroutineContext: CoroutineContext, runnable: suspend () -> Unit)
{
    viewModelScope.launch {
        withContext(coroutineContext){
            runnable()
        }
    }
}