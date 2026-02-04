package com.example.vectorscout26.utils

object TimeUtils {
    /**
     * Format milliseconds to MM:SS format
     */
    fun formatTime(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    /**
     * Format milliseconds to seconds with one decimal place
     */
    fun formatTimeSeconds(milliseconds: Long): String {
        val seconds = milliseconds / 1000.0
        return String.format("%.1f s", seconds)
    }

    /**
     * Get current timestamp
     */
    fun now(): Long = System.currentTimeMillis()
}
