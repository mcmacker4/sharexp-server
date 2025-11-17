@file:OptIn(ExperimentalMaterial3Api::class)

package es.hgg.sharexp.view.groups

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import es.hgg.sharexp.ui.theme.AppTypography
import es.hgg.sharexp.ui.theme.SharexpTheme
import es.hgg.sharexp.util.AppLog
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel


private val logger = AppLog("GroupsPage")

@Composable
fun GroupsPage(
    onNewGroupRequest: () -> Unit,
) {
    val viewModel = koinViewModel<GroupsViewModel>()
    val groups = viewModel.groupsPager.flow.collectAsLazyPagingItems()

    Scaffold(
        topBar = { GroupsTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewGroupRequest,
                content = { Icon(Icons.Filled.GroupAdd, contentDescription = "Add Group") },
            )
        }
    ) { paddingValues ->
        val pullState = rememberPullToRefreshState()

        PullToRefreshBox(
            isRefreshing = groups.loadState.refresh == LoadState.Loading,
            onRefresh = { groups.refresh() },
            state = pullState,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                items(count = groups.itemCount) { idx ->
                    groups[idx]?.let { group ->
                        GroupListItem(group.name) { logger.debug("Group ${group.name} (${group.id}) clicked") }
                    }
                }

                if (groups.loadState.append == LoadState.Loading) {
                    item {
                        CircularProgressIndicator(modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally))
                    }
                }
            }
        }
    }
}

@Composable
fun GroupListItem(
    title: String,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.Group, contentDescription = "Group", modifier = Modifier.size(42.dp))
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(style = AppTypography.titleMedium, text = title)
            }
        }
    }
}

@Preview
@Composable
private fun GroupListItemPreview() {
    GroupListItem("Group Title") {}
}

@Composable
private fun GroupsTopBar(
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
    ) {
        TopAppBar(title = { Text("Groups") })
    }
}
