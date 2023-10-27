package com.example.jmb_bms

import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock

object Errors {

    private var lastConnectionErr: ConnectionErr = ConnectionErr.NO_ERR
    private val connectingLock = ReentrantLock()
    private var hasErr: Boolean = false
    var latch = CountDownLatch(1)
    fun getLastConErrAndResetErr() : ConnectionErr
    {
        val res = lastConnectionErr
        lastConnectionErr = ConnectionErr.NO_ERR
        hasErr = false
        return res
    }
    fun hasErr(): Boolean {

        resetCountDownLatch()
        return hasErr
    }
    fun setLastConErr(err: ConnectionErr)
    {
        hasErr = true
        lastConnectionErr = err
    }

    fun resetCountDownLatch() { latch = CountDownLatch(1)}

}