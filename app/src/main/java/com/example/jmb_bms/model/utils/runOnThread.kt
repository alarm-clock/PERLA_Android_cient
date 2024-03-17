package com.example.jmb_bms.model.utils

import kotlinx.coroutines.runBlocking
import kotlin.concurrent.thread

fun runOnThread(code: suspend () -> Unit)
{
    thread {
        runBlocking{
            code()
        }
    }
}
