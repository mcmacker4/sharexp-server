package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.service.GroupService
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject


fun Route.getGroup() = get {
    val service by inject<GroupService>()
    respondEither {
        suspendTransaction { service.fetchGroupData(call.getGroupIdParam(), call.getUserPrincipal()) }
    }

}