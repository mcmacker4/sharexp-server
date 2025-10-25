package es.hgg.sharexp.server.api.groups.balance

import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.balanceApi() = route("/balance") {
    getGroupBalance()
}