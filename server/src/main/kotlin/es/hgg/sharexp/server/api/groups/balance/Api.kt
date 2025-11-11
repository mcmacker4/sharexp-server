package es.hgg.sharexp.server.api.groups.balance

import io.ktor.server.routing.*


fun Route.balanceApi() = route("/balance") {
    getGroupBalance()
}