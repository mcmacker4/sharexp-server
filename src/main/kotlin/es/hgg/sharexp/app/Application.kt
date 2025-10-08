package es.hgg.sharexp.app

import es.hgg.sharexp.api.configureApi
import es.hgg.sharexp.app.plugins.configureAuthentication
import es.hgg.sharexp.app.plugins.configureCallLogging
import es.hgg.sharexp.app.plugins.configureContentNegotiation
import es.hgg.sharexp.app.plugins.configureSessions
import es.hgg.sharexp.persistence.configureDatabase
import es.hgg.sharexp.persistence.repositories.GroupRepository
import es.hgg.sharexp.persistence.repositories.UserRepository
import es.hgg.sharexp.service.GroupService
import es.hgg.sharexp.service.UserService
import io.ktor.server.application.*
import io.ktor.server.plugins.di.*


fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

suspend fun Application.configureApplication() {
    configureDatabase()
    configureDependencyInjection()
    configurePlugins()
    configureApi()
}

fun Application.configureDependencyInjection() {
    dependencies {
        // Repositories
        provide(UserRepository::class)
        provide(GroupRepository::class)

        // Services
        provide(UserService::class)
        provide(GroupService::class)
    }
}

fun Application.configurePlugins() {
    configureCallLogging()
    configureContentNegotiation()
    configureSessions()
    configureAuthentication()
}
