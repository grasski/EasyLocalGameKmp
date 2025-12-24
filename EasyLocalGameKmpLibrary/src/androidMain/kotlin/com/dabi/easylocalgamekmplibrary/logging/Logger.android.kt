package com.dabi.easylocalgamekmplibrary.logging

import android.util.Log

/**
 * Android implementation using Android's Log class.
 */
internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    when (level) {
        LogLevel.DEBUG -> {
            if (throwable != null) Log.d(tag, message, throwable)
            else Log.d(tag, message)
        }
        LogLevel.INFO -> {
            if (throwable != null) Log.i(tag, message, throwable)
            else Log.i(tag, message)
        }
        LogLevel.WARNING -> {
            if (throwable != null) Log.w(tag, message, throwable)
            else Log.w(tag, message)
        }
        LogLevel.ERROR -> {
            if (throwable != null) Log.e(tag, message, throwable)
            else Log.e(tag, message)
        }
    }
}
