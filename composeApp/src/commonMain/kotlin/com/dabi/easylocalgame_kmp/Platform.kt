package com.dabi.easylocalgame_kmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform