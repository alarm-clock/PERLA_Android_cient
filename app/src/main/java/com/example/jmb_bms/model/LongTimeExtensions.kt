package com.example.jmb_bms.model

const val secConst = 1000
const val minConst = 60000
const val hourConst = 120000
fun Long.secondsToMilis(): Long = this * secConst
fun Long.minutesToMilis(): Long = this * minConst
fun Long.hoursToMilis(): Long = this * hourConst