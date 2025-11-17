package es.hgg.sharexp.service

import androidx.paging.PagingSource
import androidx.paging.PagingState
import es.hgg.sharexp.model.GroupInfo
import es.hgg.sharexp.util.AppLog
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class GroupsPagingSource(val groupsService: GroupsService) : PagingSource<Int, GroupInfo>() {

    private val logger = AppLog("GroupsPagingSource")

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, GroupInfo> {
        delay(1.seconds)

        val page = params.key ?: 1
        val groups = groupsService.fetchGroupsListPage(page, params.loadSize)

        logger.debug("$groups")

        return LoadResult.Page(
            data = groups,
            prevKey = null,
            nextKey = (page + 1).takeIf { groups.size == params.loadSize }
        )
    }

    override fun getRefreshKey(state: PagingState<Int, GroupInfo>): Int? = state.anchorPosition?.let { anchor ->
        val anchorPage = state.closestPageToPosition(anchor)
        anchorPage?.prevKey?.plus(1) ?: anchorPage?.prevKey?.minus(1)
    }

}