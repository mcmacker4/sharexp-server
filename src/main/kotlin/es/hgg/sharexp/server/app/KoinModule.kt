package es.hgg.sharexp.server.app

import es.hgg.sharexp.server.persistence.repositories.ExpenseRepository
import es.hgg.sharexp.server.persistence.repositories.GroupMemberRepository
import es.hgg.sharexp.server.persistence.repositories.GroupRepository
import es.hgg.sharexp.server.persistence.repositories.UserRepository
import es.hgg.sharexp.server.service.BalanceService
import es.hgg.sharexp.server.service.GroupExpenseService
import es.hgg.sharexp.server.service.GroupMemberService
import es.hgg.sharexp.server.service.GroupService
import es.hgg.sharexp.server.service.UserService
import io.ktor.server.application.*
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(KoinModule)
    }
}

val KoinModule = module {
    singleOf(::UserRepository)
    singleOf(::GroupRepository)
    singleOf(::GroupMemberRepository)
    singleOf(::ExpenseRepository)

    singleOf(::UserService)
    singleOf(::GroupService)
    singleOf(::GroupExpenseService)
    singleOf(::GroupMemberService)
    singleOf(::BalanceService)
}