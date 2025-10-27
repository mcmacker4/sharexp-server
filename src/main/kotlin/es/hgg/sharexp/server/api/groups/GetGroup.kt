package es.hgg.sharexp.server.api.groups

import es.hgg.sharexp.server.api.respondEither
import es.hgg.sharexp.server.api.getUserPrincipal
import es.hgg.sharexp.server.service.fetchGroupData
import io.ktor.server.routing.*
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction


fun Route.getGroup() = get {
    respondEither {
        suspendTransaction { fetchGroupData(call.getGroupIdParam(), call.getUserPrincipal()) }
    }
}