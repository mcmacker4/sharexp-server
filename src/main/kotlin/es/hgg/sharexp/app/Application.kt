package es.hgg.sharexp.app

import es.hgg.sharexp.api.configureApi
import es.hgg.sharexp.app.plugins.configureAuthentication
import es.hgg.sharexp.app.plugins.configureCallLogging
import es.hgg.sharexp.app.plugins.configureContentNegotiation
import es.hgg.sharexp.app.plugins.configureSessions
import es.hgg.sharexp.persistence.configureDatabase
import io.ktor.server.application.*


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.configureApplication() {
    configureDatabase()
    configurePlugins()
    configureApi()
}

fun Application.configurePlugins() {
    configureCallLogging()
    configureContentNegotiation()
    configureSessions()
    configureAuthentication()
}
