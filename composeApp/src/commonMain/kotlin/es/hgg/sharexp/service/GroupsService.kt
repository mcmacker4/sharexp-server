package es.hgg.sharexp.service

import es.hgg.sharexp.model.GroupInfo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class GroupsService(val client: HttpClient) {

    // TODO: Handle any errors
    suspend fun fetchGroupsListPage(page: Int, size: Int): List<GroupInfo> {
        return client.get("${getBaseUrl()}/groups") {
            url {
                parameter("page", page)
                parameter("size", size)
            }
        }.body()
    }

}