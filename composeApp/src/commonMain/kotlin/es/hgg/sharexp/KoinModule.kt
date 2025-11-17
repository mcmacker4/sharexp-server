package es.hgg.sharexp

import es.hgg.sharexp.service.AuthenticationService
import es.hgg.sharexp.service.GroupsService
import es.hgg.sharexp.view.groups.GroupsViewModel
import es.hgg.sharexp.view.login.LoginViewModel
import es.hgg.sharexp.view.register.RegisterViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.*
import io.ktor.serialization.kotlinx.json.json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun platformModule(): Module

fun commonModule(): Module = module {
    single {
        HttpClient {
            install(HttpCookies)
            install(ContentNegotiation) {
                json()
            }
        }
    }

    single { AuthenticationService(get()) }
    single { GroupsService(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }

    viewModel { GroupsViewModel(get()) }
}

fun initKoin() {
    startKoin {
        modules(platformModule(), commonModule())
    }
}