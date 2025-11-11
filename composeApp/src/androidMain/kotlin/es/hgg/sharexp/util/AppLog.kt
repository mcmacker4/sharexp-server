package es.hgg.sharexp.util

import android.util.Log

actual class AppLog actual constructor(val name: String?) {
    actual fun debug(msg: String) {
        Log.d(name, msg)
    }

    actual fun info(msg: String) {
        Log.i(name, msg)
    }

    actual fun warn(msg: String) {
        Log.w(name, msg)
    }

    actual fun error(msg: String) {
        Log.e(name, msg)
    }
}