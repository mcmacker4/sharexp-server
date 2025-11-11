package es.hgg.sharexp.util

actual class AppLog actual constructor(name: String?) {
    actual fun debug(msg: String) {}
    actual fun info(msg: String) {}
    actual fun warn(msg: String) {}
    actual fun error(msg: String) {}
}