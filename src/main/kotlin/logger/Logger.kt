package com.antik.utils.logger

interface Logger {
    fun debug(tag: String, msg: String)
    fun message(msg: String)
}