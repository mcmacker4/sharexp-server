package es.hgg.sharexp.util

interface AppLog {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String)
}

expect fun AppLog(name: String): AppLog