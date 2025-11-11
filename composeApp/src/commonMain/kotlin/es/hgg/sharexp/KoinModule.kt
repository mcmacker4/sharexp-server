package es.hgg.sharexp

import es.hgg.sharexp.service.AuthenticationService
import es.hgg.sharexp.view.groups.GroupsViewModel
import es.hgg.sharexp.view.login.LoginViewModel
import es.hgg.sharexp.view.register.RegisterViewModel
import io.ktor.client.*
import io.ktor.client.plugins.cookies.*
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

expect fun platformModule(): Module

fun commonModule(): Module = module {
    single {
        HttpClient {
            install(HttpCookies)
        }
    }

    single { AuthenticationService(get()) }

    viewModel { LoginViewModel(get()) }
    viewModel { RegisterViewModel(get()) }

    viewModel { GroupsViewModel() }
}

fun initKoin() {
    startKoin {
        modules(platformModule(), commonModule())
    }
}