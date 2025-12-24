package com.dabi.easylocalgamekmplibrary.logging

import platform.Foundation.NSLog

/**
 * iOS implementation using NSLog.
 */
internal actual fun platformLog(level: LogLevel, tag: String, message: String, throwable: Throwable?) {
    val levelPrefix = when (level) {
        LogLevel.DEBUG -> "DEBUG"
        LogLevel.INFO -> "INFO"
        LogLevel.WARNING -> "⚠️ WARNING"
        LogLevel.ERROR -> "❌ ERROR"
    }
    
    val fullMessage = if (throwable != null) {
        "[$levelPrefix] $tag: $message\n${throwable.stackTraceToString()}"
    } else {
        "[$levelPrefix] $tag: $message"
    }
    
    NSLog(fullMessage)
}
