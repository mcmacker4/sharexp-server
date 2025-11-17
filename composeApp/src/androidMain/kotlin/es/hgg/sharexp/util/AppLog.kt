package es.hgg.sharexp.util

import android.util.Log

private class AndroidAppLog(val name: String?) : AppLog {
    override fun debug(msg: String) {
        Log.d(name, msg)
    }

    override fun info(msg: String) {
        Log.i(name, msg)
    }

    override fun warn(msg: String) {
        Log.w(name, msg)
    }

    override fun error(msg: String) {
        Log.e(name, msg)
    }
}

actual fun AppLog(name: String): AppLog = AndroidAppLog(name)