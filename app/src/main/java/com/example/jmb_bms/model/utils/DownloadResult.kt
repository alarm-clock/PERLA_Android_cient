/**
 * @file: DownloadResult.kt
 * @author: Jozef Michal Bukas <xbukas00@stud.fit.vutbr.cz,jozefmbukas@gmail.com>
 * Description: File containing DownloadResult class
 */
package com.example.jmb_bms.model.utils

/**
 * Sealed class representing multiple download stages
 */
sealed class DownloadResult {

    /**
     * Object representing successful download
     */
    data object Success : DownloadResult()

    /**
     * Data class representing error during download
     * @param message Error message
     * @param cause [Exception] that caused fail if exception coursed fail
     */
    data class Error(val message: String, val cause: Exception? = null) : DownloadResult()

    /**
     * Data class representing active download
     * @param progress Download percentage rounded to [Int]
     */
    data class Progress(val progress: Int) : DownloadResult()

}