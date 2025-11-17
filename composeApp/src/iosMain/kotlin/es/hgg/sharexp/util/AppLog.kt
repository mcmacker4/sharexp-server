package es.hgg.sharexp.util

class IOSAppLog(name: String?) : AppLog {
    override fun debug(msg: String) = TODO()
    override fun info(msg: String) = TODO()
    override fun warn(msg: String) = TODO()
    override fun error(msg: String) = TODO()
}

actual fun AppLog(name: String): AppLog = IOSAppLog(name)