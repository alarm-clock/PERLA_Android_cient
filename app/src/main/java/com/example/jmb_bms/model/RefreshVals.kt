/**
 * @file: RefreshVals.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing RefreshVals class
 */
package com.example.jmb_bms.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Enum class with all possible location refresh values.
 * @param menuString String that will be shown in menus
 * @param delay [Duration] value that is used in location sharing
 */
enum class RefreshVals(val menuString: String, val delay: Duration) {
    S1("1s", 1.seconds ),
    S5("5s",5.seconds),
    S10("10s",10.seconds),
    S15("15s",15.seconds),
    S20("20s",20.seconds),
    S30("30s",30.seconds),
    M1("1m",1.minutes),
    M1S30("1m 30s",1.minutes + 30.seconds),
    M2("2m",2.minutes),
    M2S30("2m 30s",2.minutes + 30.seconds),
    M3("3m",3.minutes),
    M4("4m",4.minutes),
    M5("5m",5.minutes),
    M7S30("7m 30s", 7.minutes + 30.seconds),
    M10("10m",10.minutes),
    M15("15m",15.minutes),
    M20("20m",20.minutes),
    M30("30m",30.minutes),
    M45("45m",45.minutes),
    H1("1h",1.hours)
}
