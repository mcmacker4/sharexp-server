package es.hgg.sharexp.util

expect class AppLog(name: String?) {
    fun debug(msg: String)
    fun info(msg: String)
    fun warn(msg: String)
    fun error(msg: String)
}