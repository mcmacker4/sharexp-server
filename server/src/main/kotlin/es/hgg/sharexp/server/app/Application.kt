package es.hgg.sharexp.server.app

import es.hgg.sharexp.server.api.configureApi
import es.hgg.sharexp.server.app.plugins.configureAuthentication
import es.hgg.sharexp.server.app.plugins.configureCallLogging
import es.hgg.sharexp.server.app.plugins.configureContentNegotiation
import es.hgg.sharexp.server.app.plugins.configureSessions
import es.hgg.sharexp.server.persistence.configureDatabase
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
